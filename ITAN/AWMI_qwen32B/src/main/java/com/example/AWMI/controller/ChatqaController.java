package com.example.AWMI.controller;

import com.example.AWMI.common.Constants;
import com.example.AWMI.utils.ChatqaUtils;
import org.springframework.web.bind.annotation.*;
import com.example.AWMI.common.Result;
import javax.annotation.Resource;

import com.example.AWMI.service.IChatqaService;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kloudi
 * @since 2025-02-25
 */
@RestController
@CrossOrigin
@RequestMapping("/chatqa")
        public class ChatqaController {

        @Resource
        private IChatqaService chatqaService;

        @PostMapping("/addChatqa")
        public Result addChatqa(@RequestBody ChatqaUtils.ChatqaRequest request) {
                // 调用 Service 层处理数据插入并返回 chatId
                Integer chatId = chatqaService.addChatqa(request);

                // 根据插入结果返回成功或错误信息
                return chatId != null ? Result.success("数据插入成功", chatId) : Result.error(Constants.CODE_500, "插入数据失败");
        }


        /**
         * 处理前端传递的评分更新请求。
         *
         * @param request 前端传递的评分更新数据。
         * @return 返回处理结果。
         */
        @PutMapping("/updateScores")
        public Result updateScores(@RequestBody ChatqaUtils.ChatqaRequest request) {
                boolean isUpdated = chatqaService.updateScores(request);
                if (isUpdated) {
                        return Result.success("评分更新成功", null);
                } else {
                        return Result.error("400", "评分更新失败");
                }
        }

        /**
         * 处理前端 POST 请求，接收一个字符串参数，调用 Python 脚本，并返回执行结果。
         *
         * @param inputData 前端传入的字符串（JSON 格式）
         * @return Python 脚本的输出结果（封装在 Result 中）
         */
        @PostMapping("/generateAnswer")
        public Result generateAnswer(@RequestBody String inputData) {
                String result = chatqaService.generateAnswer(inputData);
                return Result.success("生成成功", result != null ? result : ""); // 避免 data 为 null
        }



}

