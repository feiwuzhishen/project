package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.entity.CommunityPost;
import com.finance.entity.PostComment;
import com.finance.entity.PostLike;
import com.finance.mapper.CommunityPostMapper;
import com.finance.mapper.PostCommentMapper;
import com.finance.mapper.PostLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommunityService {

    @Autowired
    private CommunityPostMapper postMapper;

    @Autowired
    private PostCommentMapper commentMapper;

    @Autowired
    private PostLikeMapper likeMapper;

    private static final String[] AVATAR_COLORS = {"#ed8936", "#52c41a", "#722ed1", "#1890ff", "#faad14", "#eb2f96"};

    public Map<String, Object> createPost(Long userId, String username, String title, String content, String tag) {
        Map<String, Object> result = new HashMap<>();
        try {
            CommunityPost post = new CommunityPost();
            post.setUserId(userId);
            post.setUsername(username);
            post.setTitle(title);
            post.setContent(content);
            post.setTag(tag);
            post.setTagColor(getTagColor(tag));
            post.setAvatarColor(AVATAR_COLORS[new Random().nextInt(AVATAR_COLORS.length)]);
            post.setViews(0);
            post.setComments(0);
            post.setLikes(0);
            post.setCreateTime(LocalDateTime.now());
            post.setUpdateTime(LocalDateTime.now());
            postMapper.insert(post);
            result.put("success", true);
            result.put("data", post);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getPosts(Integer page, Integer size, Long currentUserId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Page<CommunityPost> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(CommunityPost::getCreateTime);
            Page<CommunityPost> postPage = postMapper.selectPage(pageParam, wrapper);
            List<Map<String, Object>> posts = new ArrayList<>();
            for (CommunityPost post : postPage.getRecords()) {
                Map<String, Object> postMap = convertPostToMap(post);
                if (currentUserId != null) {
                    boolean liked = isUserLikedPost(post.getId(), currentUserId);
                    postMap.put("liked", liked);
                } else {
                    postMap.put("liked", false);
                }
                posts.add(postMap);
            }
            result.put("success", true);
            result.put("data", posts);
            result.put("total", postPage.getTotal());
            result.put("page", page);
            result.put("size", size);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getLatestPosts(Long since, Long currentUserId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
            if (since != null && since > 0) {
                LocalDateTime sinceTime = LocalDateTime.ofEpochSecond(since, 0, java.time.ZoneOffset.ofHours(8));
                wrapper.gt(CommunityPost::getCreateTime, sinceTime);
            }
            wrapper.orderByDesc(CommunityPost::getCreateTime);
            wrapper.last("LIMIT 20");
            List<CommunityPost> posts = postMapper.selectList(wrapper);
            List<Map<String, Object>> postList = new ArrayList<>();
            for (CommunityPost post : posts) {
                Map<String, Object> postMap = convertPostToMap(post);
                if (currentUserId != null) {
                    boolean liked = isUserLikedPost(post.getId(), currentUserId);
                    postMap.put("liked", liked);
                } else {
                    postMap.put("liked", false);
                }
                postList.add(postMap);
            }
            result.put("success", true);
            result.put("data", postList);
            result.put("timestamp", System.currentTimeMillis() / 1000);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> addComment(Long postId, Long userId, String username, String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            CommunityPost post = postMapper.selectById(postId);
            if (post == null) {
                result.put("success", false);
                result.put("error", "帖子不存在");
                return result;
            }
            PostComment comment = new PostComment();
            comment.setPostId(postId);
            comment.setUserId(userId);
            comment.setUsername(username);
            comment.setContent(content);
            comment.setAvatarColor(AVATAR_COLORS[new Random().nextInt(AVATAR_COLORS.length)]);
            comment.setCreateTime(LocalDateTime.now());
            commentMapper.insert(comment);
            post.setComments(post.getComments() + 1);
            post.setUpdateTime(LocalDateTime.now());
            postMapper.updateById(post);
            result.put("success", true);
            result.put("data", comment);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> toggleLike(Long postId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            CommunityPost post = postMapper.selectById(postId);
            if (post == null) {
                result.put("success", false);
                result.put("error", "帖子不存在");
                return result;
            }
            LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId);
            PostLike existingLike = likeMapper.selectOne(wrapper);
            boolean liked;
            if (existingLike != null) {
                likeMapper.deleteById(existingLike.getId());
                post.setLikes(Math.max(0, post.getLikes() - 1));
                liked = false;
            } else {
                PostLike like = new PostLike();
                like.setPostId(postId);
                like.setUserId(userId);
                like.setCreateTime(LocalDateTime.now());
                likeMapper.insert(like);
                post.setLikes(post.getLikes() + 1);
                liked = true;
            }
            post.setUpdateTime(LocalDateTime.now());
            postMapper.updateById(post);
            result.put("success", true);
            result.put("liked", liked);
            result.put("likes", post.getLikes());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getComments(Long postId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PostComment::getPostId, postId).orderByDesc(PostComment::getCreateTime);
            List<PostComment> comments = commentMapper.selectList(wrapper);
            result.put("success", true);
            result.put("data", comments);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private boolean isUserLikedPost(Long postId, Long userId) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId);
        return likeMapper.selectCount(wrapper) > 0;
    }

    private Map<String, Object> convertPostToMap(CommunityPost post) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", post.getId());
        map.put("userId", post.getUserId());
        map.put("author", post.getUsername());
        map.put("title", post.getTitle());
        map.put("content", post.getContent());
        map.put("tag", post.getTag());
        map.put("tagColor", post.getTagColor());
        map.put("avatarColor", post.getAvatarColor());
        map.put("views", post.getViews());
        map.put("comments", post.getComments());
        map.put("likes", post.getLikes());
        map.put("createTime", post.getCreateTime());
        map.put("updateTime", post.getUpdateTime());
        return map;
    }

    private String getTagColor(String tag) {
        Map<String, String> colorMap = new HashMap<>();
        colorMap.put("投资心得", "orange");
        colorMap.put("市场分析", "purple");
        colorMap.put("问题求助", "blue");
        colorMap.put("读书分享", "green");
        return colorMap.getOrDefault(tag, "default");
    }
}
