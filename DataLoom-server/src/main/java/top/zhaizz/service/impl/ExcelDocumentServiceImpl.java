package top.zhaizz.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.xmlbeans.impl.xb.xsdschema.All;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.zhaizz.mapper.ExcelDocumentMapper;
import top.zhaizz.mapper.ExcelSheetChunkMapper;
import top.zhaizz.mapper.ExcelSheetMapper;
import top.zhaizz.pojo.entity.ExcelSheetChunk;
import top.zhaizz.pojo.vo.AllCelldataVO;
import top.zhaizz.pojo.vo.DocumentDetailVO;
import top.zhaizz.pojo.vo.PageQueryVO;
import top.zhaizz.pojo.entity.ExcelDocument;
import top.zhaizz.pojo.entity.ExcelSheet;
import top.zhaizz.service.ExcelDocumentService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelDocumentServiceImpl implements ExcelDocumentService {
    @Autowired
    private ExcelDocumentMapper excelDocumentMapper;

    /**
     * 分页查询所有文档
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return 分页结果，包含总条数、总页数、当前页码、每页条数、记录列表
     */
    @Override
    public PageQueryVO list(int pageNum, int pageSize) {
        // 设置分页参数
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（PageHelper 会自动拦截，返回分页后的数据）
        List<ExcelDocument> list = excelDocumentMapper.list();
        // PageInfo 封装了 total, pages, pageNum 等完整分页信息
        PageInfo<ExcelDocument> pageInfo = new PageInfo<>(list);
        // 转换为统一的分页 VO 返回
        return PageQueryVO.builder()
                .total(pageInfo.getTotal())
                .pages(pageInfo.getPages())
                .current(pageInfo.getPageNum())
                .records(pageInfo.getList())
                .build();
    }

    /**
     * 重命名指定文档
     *
     * @param id   文档id
     * @param body 新文件名
     */
    @Override
    public void rename(long id, Map<String, String> body) {
        ExcelDocument excelDocument = ExcelDocument.builder().id(id).name(body.get("name")).build();
        excelDocumentMapper.updateById(excelDocument);
    }
}
