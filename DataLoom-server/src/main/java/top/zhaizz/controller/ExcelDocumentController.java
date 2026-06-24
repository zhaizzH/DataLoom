package top.zhaizz.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.zhaizz.common.Result;
import top.zhaizz.pojo.VO.ExcelDocumentDetailVo;
import top.zhaizz.pojo.VO.PageQueryVo;
import top.zhaizz.service.ExcelDocumentService;

/**
 * Excel 文档接口 — 文档 CRUD + 工作簿保存 + 单元格批量更新
 */
@RestController
@RequestMapping("/api/excel/document")
@Api("Excel 文档接口")
@Slf4j
public class ExcelDocumentController {
    @Autowired
    private ExcelDocumentService documentService;

    /**
     * 文档列表（分页，仅元数据，不含单元格数据）
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     */
    @GetMapping("/list")
    @ApiOperation("文档分页查询")
    public Result<?> list(@RequestParam(defaultValue = "1") int pageNum,
                          @RequestParam(defaultValue = "20") int pageSize) {
        log.info("页码: {} 每页条数: {}", pageNum, pageSize);
        PageQueryVo result = documentService.list(pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 文档详情 — 含各 Sheet 元信息（config / chart / images / hyperlink 等），不含 celldata
     *
     * @param id 文档 ID
     * @return 文档详情
     */
    @GetMapping("/{id}")
    @ApiOperation("文档详情查询")
    public Result<?> detail(@PathVariable long id) {
        log.info("查询文档详情: {}", id);
        ExcelDocumentDetailVo detail = documentService.detail(id);
        return Result.success(detail);
    }
}