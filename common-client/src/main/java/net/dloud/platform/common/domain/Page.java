package net.dloud.platform.common.domain;

import net.dloud.platform.common.domain.entry.PageEntry;

import java.util.Collections;
import java.util.List;

/**
 * @author QuDasheng
 * @create 2019-01-14 17:18
 **/
public class Page<T> {
    /**
     * 总条数
     */
    private Long totalNum = 0L;

    /**
     * 总页数
     */
    private Long totalPageNum = 1L;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 每页具体内容
     */
    private List<T> results;


    public static <T> Page<T> build(PageEntry pageEntry, Long totalNum) {
        final Page<T> result = new Page<>();
        if (null != pageEntry) {
            Integer pageSize = pageEntry.getPageSize();
            if (totalNum > 0) {
                Long totalPageNum = totalNum / pageSize;
                if (totalNum % pageSize != 0) {
                    totalPageNum += 1;
                }
                result.setTotalPageNum(totalPageNum);
                result.setTotalNum(totalNum);
            }
            result.setPageSize(pageSize);
        }
        result.setResults(Collections.emptyList());
        return result;
    }

    public static <T> Page<T> build(PageEntry pageEntry, Long totalNum, List<T> values) {
        final Page<T> result = build(pageEntry, totalNum);
        result.setResults(values);
        return result;
    }

    public static <T> Page<T> failed() {
        return null;
    }

    public Long getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Long totalNum) {
        this.totalNum = totalNum;
    }

    public Long getTotalPageNum() {
        return totalPageNum;
    }

    public void setTotalPageNum(Long totalPageNum) {
        this.totalPageNum = totalPageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
}
