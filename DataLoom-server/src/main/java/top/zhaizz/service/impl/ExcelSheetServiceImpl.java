package top.zhaizz.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.zhaizz.mapper.ExcelDocumentMapper;
import top.zhaizz.mapper.ExcelSheetMapper;
import top.zhaizz.pojo.entity.ExcelDocument;
import top.zhaizz.pojo.entity.ExcelSheet;
import top.zhaizz.pojo.vo.DocumentDetailVO;
import top.zhaizz.service.ExcelSheetService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static top.zhaizz.common.util.parseArrayOrEmpty;
import static top.zhaizz.common.util.parseObjectOrEmpty;

@Service
public class ExcelSheetServiceImpl implements ExcelSheetService {
    @Autowired
    private ExcelDocumentMapper excelDocumentMapper;
    @Autowired
    private ExcelSheetMapper excelSheetMapper;

    /**
     * 查询指定文档详情
     *
     * @param id 查询文档id
     * @return 文档详情
     */
    @Override
    public DocumentDetailVO detail(long id) {
        ExcelDocument document = excelDocumentMapper.getById(id);

        List<ExcelSheet> sheets = excelSheetMapper.listSheetsByDocumentId(id);

        List<Map<String, Object>> sheetInfoList = new ArrayList<>();
        for (ExcelSheet s : sheets) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("sheetId", s.getId());
            info.put("sheetIndex", s.getSheetIndex());
            info.put("sheetName", s.getSheetName());
            info.put("totalRows", s.getTotalRows());
            info.put("totalCols", s.getTotalCols());
            info.put("chunkCount", s.getChunkCount());
            info.put("active", s.getActive());

            // 组装 config（含 merge / columnlen / rowlen）
            JSONObject mergeConfig = parseObjectOrEmpty(s.getMergeConfigJson());
            JSONObject columnLen = parseObjectOrEmpty(s.getColumnLenJson());
            JSONObject rowLen = parseObjectOrEmpty(s.getRowLenJson());
            JSONObject config = parseObjectOrEmpty(s.getConfigJson());
            if (config.isEmpty()) {
                config.put("merge", mergeConfig);
                config.put("columnlen", columnLen);
                config.put("rowlen", rowLen);
            }
            info.put("config", config);
            info.put("mergeConfig", mergeConfig);
            info.put("columnLen", columnLen);
            info.put("rowLen", rowLen);

            // 超链接配置
            info.put("hyperlink", parseObjectOrEmpty(s.getHyperlinkConfigJson()));
            // 图片配置
            info.put("images", parseObjectOrEmpty(s.getImagesConfigJson()));
            // 条件格式配置
            info.put("luckysheet_conditionformat_save", parseArrayOrEmpty(s.getConditionFormatJson()));
            // 图表配置
            info.put("chart", parseArrayOrEmpty(s.getChartJson()));

            sheetInfoList.add(info);
        }

        return DocumentDetailVO.builder()
                .id(document.getId())
                .name(document.getName())
                .sheetCount(document.getSheetCount())
                .version(document.getVersion())
                .sheets(sheetInfoList)
                .build();
    }
}
