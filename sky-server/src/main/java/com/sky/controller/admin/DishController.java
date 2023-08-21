package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @description: 新增菜品
     * @author: ChenXW
     * @date: 2023/8/19 14:31
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);//后绪步骤开发

        // 清理缓存数据
        String key = "dish" + dishDTO.getCategoryId();
        cleanCache(key);

        return Result.success();
    }


    /**
     * @description: 菜品分页查询
     * @author: ChenXW
     * @date: 2023/8/19 14:58
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);//后绪步骤定义
        return Result.success(pageResult);
    }


    /**
     * @description: 菜品的批量删除
     * @author: ChenXW
     * @date: 2023/8/19 15:16
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}", ids);
        dishService.deleteBatch(ids);//后绪步骤实现

        // 将所有的菜品缓存数据清理掉
        cleanCache("dish_*");

        return Result.success();
    }


    /**
     * @description: 根据id查询菜品
     * @author: ChenXW
     * @date: 2023/8/19 17:00
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);//后绪步骤实现
        return Result.success(dishVO);
    }


    /**
     * @description: 修改菜品信息
     * @author: ChenXW
     * @date: 2023/8/19 17:07
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        // 将所有的菜品缓存数据清理掉
        cleanCache("dish_*");

        return Result.success();
    }


    /**
     * @description: 起售停售
     * @author: ChenXW
     * @date: 2023/8/21 8:23
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {

        dishService.startOrStop(status, id);

        // 将所有的菜品缓存数据清理掉
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * @description: 根据分类id查询菜品
     * @author: ChenXW
     * @date: 2023/8/21 8:46
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    // 统一清理缓存数据
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }



}
