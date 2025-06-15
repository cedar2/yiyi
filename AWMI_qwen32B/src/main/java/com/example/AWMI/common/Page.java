package com.example.AWMI.common;

import lombok.Data;

import java.util.List;

/**
 * @Description: TODO
 * @author: scott
 * @date: 2022年04月04日 18:11
 */
@Data
public class Page {

    private List list;
    private int total;

    public Page startPage(List list, Integer pageNum, Integer pageSize) {
        if (list.size() == 0) {
            return null;
        }
        Integer count = list.size(); // 记录总数
        Page page = new Page();
        page.setTotal(count);
        int pageCount = 0; // 页数
        if (count % pageSize == 0) {
            pageCount = count / pageSize;
        } else {
            pageCount = count / pageSize + 1;
        }
        int fromIndex = 0; // 开始索引
        int toIndex = 0; // 结束索引
        if (!pageNum.equals(pageCount)) {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = count;
        }
        page.setList(list.subList(fromIndex, toIndex));
        return page;
    }

}
