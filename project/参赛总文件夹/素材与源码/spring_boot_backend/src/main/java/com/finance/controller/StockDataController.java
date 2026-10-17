package com.finance.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finance.entity.StockRealtimeData;
import com.finance.service.StockDataService;
import com.finance.vo.Result;

@RestController
@RequestMapping("/api/stock")
public class StockDataController {

    private final StockDataService stockDataService;

    public StockDataController(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    @GetMapping("/realtime/{code}")
    public Result<StockRealtimeData> getRealtimeData(@PathVariable String code) {
        if (code == null || code.trim().isEmpty()) {
            return Result.error(400, "股票代码不能为空");
        }

        StockRealtimeData data = stockDataService.getRealtimeData(code);
        if (data == null) {
            return Result.error(404, "未找到股票数据: " + code);
        }

        return Result.success(data);
    }

    @GetMapping("/list")
    public Result<List<StockRealtimeData>> getStockList(@RequestParam(required = false) String codes) {
        if (codes == null || codes.trim().isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<String> codeList = Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (codeList.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<StockRealtimeData> dataList = stockDataService.getRealtimeDataList(codeList);
        return Result.success(dataList);
    }

    @GetMapping("/search")
    public Result<List<StockRealtimeData>> searchStock(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<StockRealtimeData> dataList = stockDataService.searchStock(keyword);
        return Result.success(dataList);
    }
}
