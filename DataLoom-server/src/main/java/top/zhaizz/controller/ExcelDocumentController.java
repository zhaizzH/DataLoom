package top.zhaizz.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.zhaizz.common.Result;
import top.zhaizz.pojo.vo.AllCelldataVO;
import top.zhaizz.pojo.vo.DocumentDetailVO;
import top.zhaizz.pojo.vo.PageQueryVO;
import top.zhaizz.service.ExcelDocumentService;
import top.zhaizz.service.ExcelSheetChunkService;
import top.zhaizz.service.ExcelSheetService;

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
    private ExcelDocumentService excelDocumentService;
    @Autowired
    private ExcelSheetService excelSheetService;
    @Autowired
    private ExcelSheetChunkService excelSheetChunkService;

    /**
     * 文档列表（分页，仅元数据，不含单元格数据）
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     */
    @GetMapping("/list")
    @ApiOperation("文档列表")
    public Result<?> list(@RequestParam(defaultValue = "1") int pageNum,
                          @RequestParam(defaultValue = "20") int pageSize) {
        log.info("文档列表: pageNum[{}] pageSize[{}]", pageNum, pageSize);
        PageQueryVO result = excelDocumentService.list(pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 文档详情 — 含各 Sheet 元信息（config / chart / images / hyperlink 等），不含 celldata
     *
     * @param id 文档 ID
     * @return 文档详情
     */
    @GetMapping("/{id}")
    @ApiOperation("文档详情")
    public Result<?> detail(@PathVariable long id) {
        log.info("文档详情: id[{}]", id);
        DocumentDetailVO detail = excelSheetService.detail(id);
        return Result.success(detail);
    }

    /**
     * 加载指定 Sheet 的全部 celldata（合并所有分块返回）
     * <p>
     * 适用场景：前端打开文档后一次性拉取整个 Sheet 的单元格数据。
     * 对于超大 Sheet（10 万行以上），建议评估性能后使用。
     *
     * @param id      文档 ID
     * @param sheetId Sheet ID
     * @return 全部单元格数据列表
     */
    @GetMapping("/{id}/sheet/{sheetId}/all")
    @ApiOperation("加载指定 Sheet 的全部 celldata")
    public Result<?> loadAllCelldata(@PathVariable long id, @PathVariable long sheetId) {
        log.info("加载指定 Sheet 的全部 celldata: id[{}] sheetId[{}]", id, sheetId);
        AllCelldataVO allCelldataVO = excelSheetChunkService.loadAllCelldata(id, sheetId);
        return Result.success(allCelldataVO);
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
    @PutMapping("/{id}/cells/batch")
    @ApiOperation("批量增量更新单元格")
    public Result<?> batchUpdateCells(@PathVariable long id, @RequestBody List<Map<String, Object>> updates) {
        try {
            log.info("批量增量更新单元格: id[{}]", id);
            if (updates == null || updates.isEmpty()) {
                return Result.success("没有需要保存的修改");
            }
            excelSheetChunkService.batchUpdateCells(id, updates);
            return Result.success("保存成功");
        } catch (Exception e) {
            log.error("保存失败: ", e);
            return Result.fail("保存失败: " + e.getMessage());
        }
    }

    /**
     * 重命名文档
     *
     * @param id   文档 ID
     * @param body {"name": "新名称"}
     */
    @PutMapping("/{id}/name")
    @ApiOperation("重命名文档")
    public Result<?> rename(@PathVariable long id, @RequestBody Map<String, String> body) {
        log.info("重命名文档: id[{}],name: [{}]", id, body);
        if (body == null || body.isEmpty()) {
            return Result.fail("名称不能为空");
        }
        excelDocumentService.rename(id, body);
        return Result.success(";重命名成功");
    }

    /**
     * 删除文档 — 软删除文档主记录 + 软删除 Sheet + 物理删除 Chunk
     *
     * @param id 文档ID
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除文档")
    public Result<?> delete(@PathVariable long id) {
        log.info("删除文档: id[{}]", id);
        excelDocumentService.delete(id); // 删除文档主记录
        excelSheetService.delete(id); // 软删除文档Sheet
        excelSheetChunkService.delete(id); // 物理删除Chunk
        return Result.success("删除成功");
    }
}