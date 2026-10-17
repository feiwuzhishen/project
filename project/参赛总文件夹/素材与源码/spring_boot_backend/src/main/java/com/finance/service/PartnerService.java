package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.finance.entity.StudyPartner;
import com.finance.entity.User;
import com.finance.entity.UserAsset;
import com.finance.entity.UserMessage;
import com.finance.mapper.StudyPartnerMapper;
import com.finance.mapper.UserAssetMapper;
import com.finance.mapper.UserMapper;
import com.finance.mapper.UserMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PartnerService {

    @Autowired
    private StudyPartnerMapper studyPartnerMapper;

    @Autowired
    private UserMessageMapper userMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAssetMapper userAssetMapper;

    public Map<String, Object> getMatchedPartners(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserAsset userAsset = userAssetMapper.selectOne(
                new LambdaQueryWrapper<UserAsset>().eq(UserAsset::getUserId, userId)
            );
            
            if (userAsset == null) {
                result.put("success", false);
                result.put("error", "用户资产配置不存在");
                return result;
            }

            List<UserAsset> allAssets = userAssetMapper.selectList(
                new LambdaQueryWrapper<UserAsset>().ne(UserAsset::getUserId, userId)
            );

            List<Map<String, Object>> matchedUsers = new ArrayList<>();
            
            for (UserAsset asset : allAssets) {
                int matchScore = calculateMatchScore(userAsset, asset);
                
                if (matchScore >= 50) {
                    User partner = userMapper.selectById(asset.getUserId());
                    if (partner != null) {
                        StudyPartner existingPartner = studyPartnerMapper.selectOne(
                            new LambdaQueryWrapper<StudyPartner>()
                                .eq(StudyPartner::getUserId, userId)
                                .eq(StudyPartner::getPartnerId, asset.getUserId())
                        );
                        
                        Map<String, Object> matchInfo = new HashMap<>();
                        matchInfo.put("userId", partner.getId());
                        matchInfo.put("username", partner.getUsername());
                        matchInfo.put("nickname", partner.getNickname());
                        matchInfo.put("matchScore", matchScore);
                        matchInfo.put("status", existingPartner != null ? existingPartner.getStatus() : null);
                        matchInfo.put("partnerId", existingPartner != null ? existingPartner.getId() : null);
                        matchedUsers.add(matchInfo);
                    }
                }
            }

            matchedUsers.sort((a, b) -> (Integer) b.get("matchScore") - (Integer) a.get("matchScore"));

            result.put("data", matchedUsers);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private int calculateMatchScore(UserAsset asset1, UserAsset asset2) {
        int score = 0;
        
        int stockDiff = Math.abs(asset1.getStockRatio() - asset2.getStockRatio());
        int fundDiff = Math.abs(asset1.getFundRatio() - asset2.getFundRatio());
        int bondDiff = Math.abs(asset1.getBondRatio() - asset2.getBondRatio());
        int etfDiff = Math.abs(asset1.getEtfRatio() - asset2.getEtfRatio());
        int derivativesDiff = Math.abs(asset1.getDerivativesRatio() - asset2.getDerivativesRatio());
        int cashDiff = Math.abs(asset1.getCashRatio() - asset2.getCashRatio());
        
        int totalDiff = stockDiff + fundDiff + bondDiff + etfDiff + derivativesDiff + cashDiff;
        
        score = 100 - totalDiff;
        
        return Math.max(0, Math.min(100, score));
    }

    public Map<String, Object> sendPartnerRequest(Long userId, Long partnerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            StudyPartner existingRequest = studyPartnerMapper.selectOne(
                new LambdaQueryWrapper<StudyPartner>()
                    .eq(StudyPartner::getUserId, userId)
                    .eq(StudyPartner::getPartnerId, partnerId)
            );
            
            if (existingRequest != null) {
                result.put("success", false);
                result.put("error", "已经发送过好友请求");
                return result;
            }

            UserAsset userAsset = userAssetMapper.selectOne(
                new LambdaQueryWrapper<UserAsset>().eq(UserAsset::getUserId, userId)
            );
            UserAsset partnerAsset = userAssetMapper.selectOne(
                new LambdaQueryWrapper<UserAsset>().eq(UserAsset::getUserId, partnerId)
            );
            
            int matchScore = 0;
            if (userAsset != null && partnerAsset != null) {
                matchScore = calculateMatchScore(userAsset, partnerAsset);
            }

            StudyPartner partner = new StudyPartner();
            partner.setUserId(userId);
            partner.setPartnerId(partnerId);
            partner.setMatchScore(matchScore);
            partner.setStatus("pending");
            partner.setCreatedAt(LocalDateTime.now());
            studyPartnerMapper.insert(partner);

            result.put("success", true);
            result.put("message", "好友请求已发送");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> acceptPartnerRequest(Long requestId) {
        Map<String, Object> result = new HashMap<>();
        try {
            StudyPartner partnerRequest = studyPartnerMapper.selectById(requestId);
            
            if (partnerRequest == null) {
                result.put("success", false);
                result.put("error", "好友请求不存在");
                return result;
            }

            partnerRequest.setStatus("accepted");
            studyPartnerMapper.updateById(partnerRequest);

            StudyPartner reversePartner = new StudyPartner();
            reversePartner.setUserId(partnerRequest.getPartnerId());
            reversePartner.setPartnerId(partnerRequest.getUserId());
            reversePartner.setMatchScore(partnerRequest.getMatchScore());
            reversePartner.setStatus("accepted");
            reversePartner.setCreatedAt(LocalDateTime.now());
            studyPartnerMapper.insert(reversePartner);

            result.put("success", true);
            result.put("message", "已接受好友请求");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> rejectPartnerRequest(Long requestId) {
        Map<String, Object> result = new HashMap<>();
        try {
            StudyPartner partnerRequest = studyPartnerMapper.selectById(requestId);
            
            if (partnerRequest == null) {
                result.put("success", false);
                result.put("error", "好友请求不存在");
                return result;
            }

            studyPartnerMapper.deleteById(requestId);

            result.put("success", true);
            result.put("message", "已拒绝好友请求");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getPartnerList(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<StudyPartner> partners = studyPartnerMapper.selectList(
                new LambdaQueryWrapper<StudyPartner>()
                    .eq(StudyPartner::getUserId, userId)
                    .eq(StudyPartner::getStatus, "accepted")
            );

            List<Map<String, Object>> partnerList = new ArrayList<>();
            for (StudyPartner partner : partners) {
                User partnerUser = userMapper.selectById(partner.getPartnerId());
                if (partnerUser != null) {
                    Map<String, Object> partnerInfo = new HashMap<>();
                    partnerInfo.put("id", partner.getId());
                    partnerInfo.put("partnerId", partnerUser.getId());
                    partnerInfo.put("username", partnerUser.getUsername());
                    partnerInfo.put("nickname", partnerUser.getNickname());
                    partnerInfo.put("matchScore", partner.getMatchScore());
                    partnerInfo.put("createdAt", partner.getCreatedAt());
                    partnerList.add(partnerInfo);
                }
            }

            result.put("data", partnerList);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getPendingRequests(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<StudyPartner> pendingRequests = studyPartnerMapper.selectList(
                new LambdaQueryWrapper<StudyPartner>()
                    .eq(StudyPartner::getPartnerId, userId)
                    .eq(StudyPartner::getStatus, "pending")
            );

            List<Map<String, Object>> requestList = new ArrayList<>();
            for (StudyPartner request : pendingRequests) {
                User requester = userMapper.selectById(request.getUserId());
                if (requester != null) {
                    Map<String, Object> requestInfo = new HashMap<>();
                    requestInfo.put("id", request.getId());
                    requestInfo.put("requesterId", requester.getId());
                    requestInfo.put("username", requester.getUsername());
                    requestInfo.put("nickname", requester.getNickname());
                    requestInfo.put("matchScore", request.getMatchScore());
                    requestInfo.put("createdAt", request.getCreatedAt());
                    requestList.add(requestInfo);
                }
            }

            result.put("data", requestList);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> sendMessage(Long senderId, Long receiverId, String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            StudyPartner partnership = studyPartnerMapper.selectOne(
                new LambdaQueryWrapper<StudyPartner>()
                    .eq(StudyPartner::getUserId, senderId)
                    .eq(StudyPartner::getPartnerId, receiverId)
                    .eq(StudyPartner::getStatus, "accepted")
            );
            
            if (partnership == null) {
                result.put("success", false);
                result.put("error", "你们还不是伙伴关系");
                return result;
            }

            UserMessage message = new UserMessage();
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setContent(content);
            message.setIsRead(false);
            message.setCreatedAt(LocalDateTime.now());
            userMessageMapper.insert(message);

            result.put("success", true);
            result.put("message", "消息发送成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getMessages(Long userId, Long partnerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<UserMessage> messages = userMessageMapper.selectList(
                new LambdaQueryWrapper<UserMessage>()
                    .and(wrapper -> wrapper
                        .eq(UserMessage::getSenderId, userId).eq(UserMessage::getReceiverId, partnerId)
                        .or()
                        .eq(UserMessage::getSenderId, partnerId).eq(UserMessage::getReceiverId, userId)
                    )
                    .orderByAsc(UserMessage::getCreatedAt)
            );

            userMessageMapper.update(null,
                new LambdaUpdateWrapper<UserMessage>()
                    .eq(UserMessage::getSenderId, partnerId)
                    .eq(UserMessage::getReceiverId, userId)
                    .eq(UserMessage::getIsRead, false)
                    .set(UserMessage::getIsRead, true)
            );

            List<Map<String, Object>> messageList = new ArrayList<>();
            for (UserMessage msg : messages) {
                Map<String, Object> msgInfo = new HashMap<>();
                msgInfo.put("id", msg.getId());
                msgInfo.put("senderId", msg.getSenderId());
                msgInfo.put("receiverId", msg.getReceiverId());
                msgInfo.put("content", msg.getContent());
                msgInfo.put("isRead", msg.getIsRead());
                msgInfo.put("createdAt", msg.getCreatedAt());
                messageList.add(msgInfo);
            }

            result.put("data", messageList);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getUnreadCount(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long count = userMessageMapper.selectCount(
                new LambdaQueryWrapper<UserMessage>()
                    .eq(UserMessage::getReceiverId, userId)
                    .eq(UserMessage::getIsRead, false)
            );

            result.put("count", count);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
