package top.zhaizz.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.zhaizz.common.Result;
import top.zhaizz.pojo.vo.DocumentDetailVo;
import top.zhaizz.pojo.vo.PageQueryVo;
import top.zhaizz.pojo.vo.SheetChunkLoadAllCelldataVO;
import top.zhaizz.service.ExcelDocumentService;

import java.util.List;
import java.util.Map;

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
     * 文档详情 — 含各 Sheet 元信息（config / chart / images / hyperlink 等），不含 celldata
     * <p>
     * 前端打开文档时先调此接口获取 Sheet 列表和 chunkCount，
     * 再调 all 接口加载全量 celldata。
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return 分页结果，包含总条数、总页数、当前页码、每页条数、记录列表
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
     * 加载指定 Sheet 的全部 celldata（合并所有分块返回）
     * <p>
     * 适用场景：前端打开文档后一次性拉取整个 Sheet 的单元格数据。
     * 对于超大 Sheet（10 万行以上），建议评估性能后使用。
     *
     * @param id 文档 ID
     * @return 文档详情
     */
    @GetMapping("/{id}")
    @ApiOperation("文档详情查询")
    public Result<?> detail(@PathVariable long id) {
        try {
            log.info("查询文档id: {}", id);
            DocumentDetailVo detail = documentService.detail(id);
            return Result.success(detail);
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.fail("文档加载失败: " + e.getMessage());
        }
    }

    /**
     * 加载指定 Sheet 的全部 celldata（合并所有分块返回）
     *
     * @param id      文档 ID
     * @param sheetId Sheet ID
     * @return 全部 celldata 列表
     */
    @GetMapping("/{id}/sheet/{sheetId}/all")
    @ApiOperation("指定 Sheet 查询")
    public Result<?> loadAllCelldata(@PathVariable long id, @PathVariable long sheetId) {
        try {
            log.info("文档 ID: {},Sheet ID: {}", id, sheetId);
            SheetChunkLoadAllCelldataVO result = documentService.loadAllCelldata(id, sheetId);
            if (result == null) {
                return Result.fail(404, "Sheet 不存在");
            }
            return Result.success(result);
        } catch (Exception e) {
            log.warn("解析 chunk[{}] 失败: {}", sheetId, e.getMessage());
            return Result.fail("加载全部 celldata 失败: " + e.getMessage());
        }
    }

    /**
     * 批量增量更新单元格 — 按 Chunk 分组后逐块回写，只影响相关分块
     * <p>
     * 前端保存时，如果只有单元格内容变化（无结构 / 图片 / 图表变更），
     * 优先走此接口，避免全量重建所有分块。
     *
     * @param id      文档 ID
     * @param updates 单元格修改列表：[{"sheetId": 1, "r": 0, "c": 1, "v": {...}}, ...]
     */
    @PostMapping("/{id}/cells/batch")
    @ApiOperation("批量更新单元格")
    public Result<?> batchUpdateCells(@PathVariable long id, @RequestBody List<Map<String, Object>> updates) {
        try {
            log.info("批量更新文档ID: {},单元格参数: {}", id, updates);
            if (updates == null || updates.isEmpty()) {
                return Result.success("没有需要保存的修改");
            }
            documentService.batchUpdateCells(id, updates);
            return Result.success("保存成功");
        } catch (Exception e) {
            log.error("批量更新单元格失败: doc={}, error={}", id, e.getMessage());
            return Result.fail("保存失败: " + e.getMessage());
        }
    }
}