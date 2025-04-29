
package com.example.AWMI.controller;

import org.springframework.web.bind.annotation.*;
import com.example.AWMI.common.Result;
import javax.annotation.Resource;
import java.util.List;

import com.example.AWMI.service.IModelService;
import com.example.AWMI.entity.Model;

import org.springframework.stereotype.Controller;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kloudi
 * @since 2025-02-25
 */
@Controller
@RequestMapping("//model")
        public class ModelController {
    
@Resource
private IModelService modelService;

// 新增或者更新
@PostMapping
public Result save(@RequestBody Model model) {
        return Result.success(modelService.saveOrUpdate(model));
        }

//根据id删除
@DeleteMapping("/{id}")
public Result delete(@PathVariable Integer id) {
        return Result.success(modelService.removeById(id));
        }

//根据id批量删除
@PostMapping("/del/batch")
public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(modelService.removeByIds(ids));
        }

//查找所有
@GetMapping
public Result findAll() {
        return Result.success(modelService.list());
        }

//根据id查找
@GetMapping("/{id}")
public Result findOne(@PathVariable Integer id) {
        return Result.success(modelService.getById(id));
        }

                                    }

