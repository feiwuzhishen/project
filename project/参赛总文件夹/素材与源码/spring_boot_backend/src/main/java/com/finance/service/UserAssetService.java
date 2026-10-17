package com.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.entity.User;
import com.finance.entity.UserAsset;
import com.finance.entity.UserStockHold;
import com.finance.mapper.UserAssetMapper;
import com.finance.mapper.UserMapper;
import com.finance.mapper.UserStockHoldMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserAssetService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAssetMapper userAssetMapper;

    @Autowired
    private UserStockHoldMapper userStockHoldMapper;

    public Map<String, Object> getUserAssetConfig(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserAsset asset = userAssetMapper.selectOne(
                new LambdaQueryWrapper<UserAsset>().eq(UserAsset::getUserId, userId)
            );
            if (asset != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("stockRatio", asset.getStockRatio());
                data.put("fundRatio", asset.getFundRatio());
                data.put("bondRatio", asset.getBondRatio());
                data.put("etfRatio", asset.getEtfRatio());
                data.put("derivativesRatio", asset.getDerivativesRatio());
                data.put("cashRatio", asset.getCashRatio());
                result.put("data", data);
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("error", "用户资产配置不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getUserStockHolds(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<UserStockHold> holds = userStockHoldMapper.selectList(
                new LambdaQueryWrapper<UserStockHold>().eq(UserStockHold::getUserId, userId)
            );
            result.put("data", holds);
            result.put("success", true);
            result.put("count", holds.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getUserInfo(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("username", user.getUsername());
                data.put("nickname", user.getNickname());
                data.put("avatar", user.getAvatar());
                data.put("gender", user.getGender());
                data.put("email", user.getEmail());
                data.put("phone", user.getPhone());
                data.put("createTime", user.getCreateTime());
                result.put("data", data);
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("error", "用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getAllUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> users = userMapper.selectList(null);
            result.put("data", users);
            result.put("success", true);
            result.put("count", users.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getUserDetail(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userMapper.selectById(userId);
            UserAsset asset = userAssetMapper.selectOne(
                new LambdaQueryWrapper<UserAsset>().eq(UserAsset::getUserId, userId)
            );
            List<UserStockHold> holds = userStockHoldMapper.selectList(
                new LambdaQueryWrapper<UserStockHold>().eq(UserStockHold::getUserId, userId)
            );

            if (user != null) {
                Map<String, Object> data = new HashMap<>();
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("nickname", user.getNickname());
                userMap.put("avatar", user.getAvatar());
                userMap.put("gender", user.getGender());
                userMap.put("email", user.getEmail());
                userMap.put("phone", user.getPhone());
                userMap.put("createTime", user.getCreateTime());
                data.put("user", userMap);
                if (asset != null) {
                    data.put("asset", Map.of(
                        "stockRatio", asset.getStockRatio(),
                        "fundRatio", asset.getFundRatio(),
                        "bondRatio", asset.getBondRatio(),
                        "etfRatio", asset.getEtfRatio(),
                        "derivativesRatio", asset.getDerivativesRatio(),
                        "cashRatio", asset.getCashRatio()
                    ));
                }
                data.put("stockHolds", holds);
                result.put("data", data);
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("error", "用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> updateUserInfo(Integer userId, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("error", "用户不存在");
                return result;
            }

            if (params.containsKey("nickname")) {
                user.setNickname((String) params.get("nickname"));
            }
            if (params.containsKey("avatar")) {
                user.setAvatar((String) params.get("avatar"));
            }
            if (params.containsKey("gender")) {
                user.setGender((String) params.get("gender"));
            }
            if (params.containsKey("email")) {
                user.setEmail((String) params.get("email"));
            }
            if (params.containsKey("phone")) {
                user.setPhone((String) params.get("phone"));
            }

            userMapper.updateById(user);

            result.put("success", true);
            result.put("message", "用户信息更新成功");
            result.put("data", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "nickname", user.getNickname(),
                "avatar", user.getAvatar(),
                "gender", user.getGender(),
                "email", user.getEmail(),
                "phone", user.getPhone()
            ));
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
