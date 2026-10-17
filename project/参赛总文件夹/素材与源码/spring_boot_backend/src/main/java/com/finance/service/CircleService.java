package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.entity.CircleMember;
import com.finance.entity.CirclePost;
import com.finance.entity.StudyCircle;
import com.finance.entity.User;
import com.finance.mapper.CircleMemberMapper;
import com.finance.mapper.CirclePostMapper;
import com.finance.mapper.StudyCircleMapper;
import com.finance.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CircleService {

    @Autowired
    private StudyCircleMapper studyCircleMapper;

    @Autowired
    private CircleMemberMapper circleMemberMapper;

    @Autowired
    private CirclePostMapper circlePostMapper;

    @Autowired
    private UserMapper userMapper;

    public Map<String, Object> createCircle(String name, String description, Integer creatorId, Boolean isPublic) {
        Map<String, Object> result = new HashMap<>();
        try {
            StudyCircle circle = new StudyCircle();
            circle.setName(name);
            circle.setDescription(description);
            circle.setCreatorId(creatorId);
            circle.setIsPublic(isPublic);
            circle.setMemberCount(1);
            circle.setCreatedAt(LocalDateTime.now());
            studyCircleMapper.insert(circle);

            CircleMember member = new CircleMember();
            member.setCircleId(circle.getId());
            member.setUserId(creatorId);
            member.setRole("admin");
            member.setJoinedAt(LocalDateTime.now());
            circleMemberMapper.insert(member);

            result.put("data", circle);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getCircleList(String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            LambdaQueryWrapper<StudyCircle> wrapper = new LambdaQueryWrapper<>();
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like(StudyCircle::getName, keyword)
                       .or()
                       .like(StudyCircle::getDescription, keyword);
            }
            wrapper.orderByDesc(StudyCircle::getCreatedAt);
            List<StudyCircle> circles = studyCircleMapper.selectList(wrapper);
            result.put("data", circles);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getCircleDetail(Integer circleId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            StudyCircle circle = studyCircleMapper.selectById(circleId);
            if (circle == null) {
                result.put("success", false);
                result.put("error", "圈子不存在");
                return result;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", circle.getId());
            data.put("name", circle.getName());
            data.put("description", circle.getDescription());
            data.put("creatorId", circle.getCreatorId());
            data.put("isPublic", circle.getIsPublic());
            data.put("memberCount", circle.getMemberCount());
            data.put("createdAt", circle.getCreatedAt());

            LambdaQueryWrapper<CircleMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(CircleMember::getCircleId, circleId);
            List<CircleMember> members = circleMemberMapper.selectList(memberWrapper);

            List<Map<String, Object>> memberList = new ArrayList<>();
            for (CircleMember m : members) {
                User user = userMapper.selectById(m.getUserId());
                if (user != null) {
                    Map<String, Object> memberInfo = new HashMap<>();
                    memberInfo.put("id", user.getId());
                    memberInfo.put("nickname", user.getNickname());
                    memberInfo.put("username", user.getUsername());
                    memberInfo.put("role", m.getRole());
                    memberInfo.put("joinedAt", m.getJoinedAt());
                    memberList.add(memberInfo);
                }
            }
            data.put("members", memberList);

            if (userId != null) {
                LambdaQueryWrapper<CircleMember> userMemberWrapper = new LambdaQueryWrapper<>();
                userMemberWrapper.eq(CircleMember::getCircleId, circleId)
                                 .eq(CircleMember::getUserId, userId);
                CircleMember userMember = circleMemberMapper.selectOne(userMemberWrapper);
                data.put("isMember", userMember != null);
                data.put("userRole", userMember != null ? userMember.getRole() : null);
            } else {
                data.put("isMember", false);
                data.put("userRole", null);
            }

            result.put("data", data);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> joinCircle(Integer circleId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            StudyCircle circle = studyCircleMapper.selectById(circleId);
            if (circle == null) {
                result.put("success", false);
                result.put("error", "圈子不存在");
                return result;
            }

            LambdaQueryWrapper<CircleMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CircleMember::getCircleId, circleId)
                   .eq(CircleMember::getUserId, userId);
            CircleMember existingMember = circleMemberMapper.selectOne(wrapper);
            if (existingMember != null) {
                result.put("success", false);
                result.put("error", "已经是圈子成员");
                return result;
            }

            CircleMember member = new CircleMember();
            member.setCircleId(circleId);
            member.setUserId(userId);
            member.setRole("member");
            member.setJoinedAt(LocalDateTime.now());
            circleMemberMapper.insert(member);

            circle.setMemberCount(circle.getMemberCount() + 1);
            studyCircleMapper.updateById(circle);

            result.put("success", true);
            result.put("message", circle.getIsPublic() ? "加入成功" : "申请已提交，等待审核");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> leaveCircle(Integer circleId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            StudyCircle circle = studyCircleMapper.selectById(circleId);
            if (circle == null) {
                result.put("success", false);
                result.put("error", "圈子不存在");
                return result;
            }

            if (circle.getCreatorId().equals(userId)) {
                result.put("success", false);
                result.put("error", "圈主不能退出圈子");
                return result;
            }

            LambdaQueryWrapper<CircleMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CircleMember::getCircleId, circleId)
                   .eq(CircleMember::getUserId, userId);
            CircleMember member = circleMemberMapper.selectOne(wrapper);
            if (member == null) {
                result.put("success", false);
                result.put("error", "不是圈子成员");
                return result;
            }

            circleMemberMapper.deleteById(member.getId());

            circle.setMemberCount(Math.max(0, circle.getMemberCount() - 1));
            studyCircleMapper.updateById(circle);

            result.put("success", true);
            result.put("message", "退出成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> createPost(Integer circleId, Integer userId, String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            LambdaQueryWrapper<CircleMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CircleMember::getCircleId, circleId)
                   .eq(CircleMember::getUserId, userId);
            CircleMember member = circleMemberMapper.selectOne(wrapper);
            if (member == null) {
                result.put("success", false);
                result.put("error", "不是圈子成员，无法发帖");
                return result;
            }

            CirclePost post = new CirclePost();
            post.setCircleId(circleId);
            post.setUserId(userId);
            post.setContent(content);
            post.setCreatedAt(LocalDateTime.now());
            circlePostMapper.insert(post);

            result.put("data", post);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getCirclePosts(Integer circleId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LambdaQueryWrapper<CirclePost> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CirclePost::getCircleId, circleId)
                   .orderByDesc(CirclePost::getCreatedAt);
            List<CirclePost> posts = circlePostMapper.selectList(wrapper);

            List<Map<String, Object>> postList = new ArrayList<>();
            for (CirclePost post : posts) {
                User user = userMapper.selectById(post.getUserId());
                Map<String, Object> postInfo = new HashMap<>();
                postInfo.put("id", post.getId());
                postInfo.put("content", post.getContent());
                postInfo.put("createdAt", post.getCreatedAt());
                if (user != null) {
                    postInfo.put("userId", user.getId());
                    postInfo.put("nickname", user.getNickname());
                    postInfo.put("username", user.getUsername());
                }
                postList.add(postInfo);
            }

            result.put("data", postList);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
