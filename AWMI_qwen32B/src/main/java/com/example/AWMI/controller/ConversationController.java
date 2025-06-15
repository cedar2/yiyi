
package com.example.AWMI.controller;

import org.springframework.web.bind.annotation.*;
import com.example.AWMI.common.Result;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import com.example.AWMI.service.IConversationService;
import com.example.AWMI.entity.Conversation;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author julie
 * @since 2025-04-16
 */
@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Resource
    private IConversationService conversationService;


    @PostMapping("/addConversation")
    public Result addConversation(@RequestBody Conversation conversation) {
        boolean success = conversationService.save(conversation);
        if (success) {
            return Result.success("新增对话成功", conversation);
        } else {
            return Result.error("500", "新增对话失败");
        }
    }


    @GetMapping("/getConversationsByUserId/{userid}")
    public Result getConversationsByUserId(@PathVariable Integer userid) {
        List<Conversation> list = conversationService.lambdaQuery()
                .eq(Conversation::getUserid, userid)
                .orderByDesc(Conversation::getId)  // 按 id 降序排列
                .list();
        return Result.success("查询成功", list);
    }
    @PostMapping("/pin/{conversationId}")
    public Result pinConversation(@PathVariable Integer conversationId, @RequestBody Map<String, Boolean> request) {
        Boolean pinned = request.get("pinned");
        boolean success = conversationService.pinConversation(conversationId, pinned);
        if (success) {
            return Result.success("操作成功");
        } else {
            return Result.error("500", "操作失败");
        }
    }

    @PostMapping("/rename/{conversationId}")
    public Result renameConversation(@PathVariable Integer conversationId, @RequestBody String newName) {
        String input=newName;
        if (input != null && input.startsWith("\"") && input.endsWith("\"") && input.length() >= 2) {
            newName= input.substring(1, input.length() - 1);
        }
        boolean success = conversationService.renameConversation(conversationId, newName);
        if (success) {
            return Result.success("重命名成功");
        } else {
            return Result.error("500", "重命名失败");
        }
    }

    @DeleteMapping("/delete/{conversationId}")
    public Result deleteConversation(@PathVariable Integer conversationId) {
        boolean success = conversationService.removeById(conversationId);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("500", "删除失败");
        }
    }
}

