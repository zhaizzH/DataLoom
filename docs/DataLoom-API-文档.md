# DataLoom REST API 文档

> 版本：v1.0.0 | 最后更新：2026-06-23
>
> DataLoom 是一个基于 Luckysheet 的在线 Excel 协作编辑平台，提供 Excel 文件的上传、解析、在线编辑（单元格/格式/图表/图片）、保存和导出功能。

---

## 目录

1. [接口概览](#1-接口概览)
2. [通用规范](#2-通用规范)
3. [接口详情](#3-接口详情)
   - 3.1 [上传文件](#31-上传文件-post-apiexcelupload)
   - 3.2 [文档列表](#32-文档列表-get-apiexceldocumentlist)
   - 3.3 [文档详情](#33-文档详情-get-apiexceldocumentid)
   - 3.4 [加载单元格数据](#34-加载全部单元格数据-get-apiexceldocumentidsheetsheetidall)
   - 3.5 [批量更新单元格](#35-批量增量更新单元格-put-apiexceldocumentidcellsbatch)
   - 3.6 [全量保存工作簿](#36-全量快照保存工作簿-put-apiexceldocumentidworkbook)
   - 3.7 [重命名文档](#37-重命名文档-put-apiexceldocumentidname)
   - 3.8 [删除文档](#38-删除文档-delete-apiexceldocumentid)
   - 3.9 [健康检查](#39-健康检查-get-仅-python-后端)
4. [数据表结构](#4-数据表结构)
5. [附录](#5-附录)

---

## 1. 接口概览

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | `POST` | `/api/excel/upload` | 上传 Excel 文件（.xlsx/.xls） |
| 2 | `GET` | `/api/excel/document/list` | 分页获取文档列表 |
| 3 | `GET` | `/api/excel/document/{id}` | 获取文档详情及所有 Sheet 元信息 |
| 4 | `GET` | `/api/excel/document/{id}/sheet/{sheetId}/all` | 加载指定 Sheet 的全部单元格数据 |
| 5 | `PUT` | `/api/excel/document/{id}/cells/batch` | 批量增量更新单元格 |
| 6 | `PUT` | `/api/excel/document/{id}/workbook` | 全量快照保存工作簿 |
| 7 | `PUT` | `/api/excel/document/{id}/name` | 重命名文档 |
| 8 | `DELETE` | `/api/excel/document/{id}` | 删除文档 |
| 9 | `GET` | `/` | 健康检查（仅 Python 后端） |

---

## 2. 通用规范

### 2.1 环境信息

| 项目 | 值 |
|------|-----|
| **Base URL** | `http://localhost:9191`（开发环境） |
| **Java 后端** | Spring Boot 2.1.15，端口 9191 |
| **Python 后端** | FastAPI 0.110+，端口 9191（备选实现，API 完全兼容） |
| **前端** | Vue 3 + Vite，端口 8081 |
| **数据代理** | Nginx 代理 `/api/*` → 后端容器 |

### 2.2 统一响应格式

所有接口响应遵循统一的 `ApiResponse` 结构：

```json
{
  "code": 200,
  "success": true,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | HTTP 状态码（200=成功，404=未找到，500=服务器错误） |
| `success` | boolean | 是否成功 |
| `message` | string | 提示信息 |
| `data` | object/null | 响应数据体 |

错误示例：

```json
{
  "code": 500,
  "success": false,
  "message": "保存失败: xxx",
  "data": null
}
```

### 2.3 分页请求参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `pageNum` | int | 1 | 页码，从 1 开始 |
| `pageSize` | int | 20 | 每页条数 |

### 2.4 分页响应结构

```json
{
  "total": 100,
  "pages": 5,
  "current": 1,
  "records": []
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `total` | int | 总记录数 |
| `pages` | int | 总页数 |
| `current` | int | 当前页码 |
| `records` | array | 当前页数据列表 |

### 2.5 跨域

后端允许所有来源跨域访问（`Access-Control-Allow-Origin: *`），路径限制在 `/api/**`。

---

## 3. 接口详情

---

### 3.1 上传文件 `POST /api/excel/upload`

上传 Excel 文件并解析入库。后台使用 **POI**（Java）或 **openpyxl**（Python）进行流式解析，按 **1000 行/块**分块存储到数据库。

#### 请求

```
POST /api/excel/upload
Content-Type: multipart/form-data
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `file` | File | 是 | Excel 文件，支持 `.xlsx` / `.xls`，最大 100MB |

#### 处理流程

1. 保存原始文件到本地磁盘（`./upload` 目录）
2. 创建文档主记录（获取文档 ID）
3. 流式解析 Excel，按 1000 行/块分块写入数据库
4. 更新文档的 Sheet 数量与名称列表
5. 返回文档 ID 和 Sheet 元信息（**不含 celldata**）

#### 成功响应（200）

```json
{
  "code": 200,
  "success": true,
  "message": "上传成功",
  "data": {
    "documentId": 1,
    "name": "example.xlsx",
    "sheetCount": 2,
    "sheets": [
      {
        "sheetId": 1,
        "sheetIndex": 0,
        "sheetName": "Sheet1",
        "totalRows": 1000,
        "totalCols": 26,
        "chunkCount": 1,
        "active": 1
      },
      {
        "sheetId": 2,
        "sheetIndex": 1,
        "sheetName": "Sheet2",
        "totalRows": 500,
        "totalCols": 20,
        "chunkCount": 1,
        "active": 0
      }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `documentId` | long | 文档唯一 ID |
| `name` | string | 原始文件名 |
| `sheetCount` | int | Sheet 数量 |
| `sheets` | array | Sheet 元信息列表（见下方） |
| sheets[].`sheetId` | long | Sheet 唯一 ID |
| sheets[].`sheetIndex` | int | Sheet 在文档中的序号（0 起始） |
| sheets[].`sheetName` | string | Sheet 名称 |
| sheets[].`totalRows` | int | 总行数 |
| sheets[].`totalCols` | int | 总列数 |
| sheets[].`chunkCount` | int | 分块数量 |
| sheets[].`active` | int | 是否为活动 Sheet（1=是，0=否） |

#### 错误响应（500）

```json
{
  "code": 500,
  "success": false,
  "message": "上传失败: 文件格式不支持",
  "data": null
}
```

---

### 3.2 文档列表 `GET /api/excel/document/list`

分页获取文档列表，仅返回元数据（不含单元格数据）。

#### 请求

```
GET /api/excel/document/list?pageNum=1&pageSize=20
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `pageNum` | int | 否 | 1 | 页码 |
| `pageSize` | int | 否 | 20 | 每页条数 |

#### 成功响应（200）

```json
{
  "code": 200,
  "success": true,
  "message": "success",
  "data": {
    "total": 10,
    "pages": 1,
    "current": 1,
    "records": [
      {
        "id": 1,
        "name": "example.xlsx",
        "sheetCount": 3,
        "sheetNames": "[\"Sheet1\",\"Sheet2\",\"Sheet3\"]",
        "version": 1,
        "fileSize": 102400,
        "createTime": "2026-06-23T10:00:00",
        "updateTime": "2026-06-23T12:00:00"
      }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 文档 ID |
| `name` | string | 文档名称 |
| `sheetCount` | int | Sheet 数量 |
| `sheetNames` | string | Sheet 名称的 JSON 数组字符串，如 `["Sheet1","Sheet2"]` |
| `version` | long | 乐观锁版本号 |
| `fileSize` | long | 文件大小（字节） |
| `createTime` | datetime | 创建时间 |
| `updateTime` | datetime | 更新时间 |

> 排序规则：按 `updateTime` **降序**排列（最新的在最前）。

---

### 3.3 文档详情 `GET /api/excel/document/{id}`

获取文档详情及所有 Sheet 的完整元配置，**不含单元格数据**。

前端打开文档时，先调此接口获取 Sheet 列表和配置，再根据需要加载 celldata。

#### 请求

```
GET /api/excel/document/{id}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 文档 ID（路径参数） |

#### 成功响应（200）

```json
{
  "code": 200,
  "success": true,
  "message": "success",
  "data": {
    "id": 1,
    "name": "example.xlsx",
    "sheetCount": 2,
    "version": 1,
    "sheets": [
      {
        "sheetId": 1,
        "sheetIndex": 0,
        "sheetName": "Sheet1",
        "totalRows": 1000,
        "totalCols": 26,
        "chunkCount": 1,
        "active": 1,
        "config": {
          "merge": {
            "0_0": { "r": 0, "c": 0, "rs": 2, "cs": 2 }
          },
          "columnlen": {
            "0": 100,
            "1": 150
          },
          "rowlen": {
            "0": 30
          }
        },
        "mergeConfig": {
          "0_0": { "r": 0, "c": 0, "rs": 2, "cs": 2 }
        },
        "columnLen": { "0": 100, "1": 150 },
        "rowLen": { "0": 30 },
        "hyperlink": {
          "0_0": { "url": "https://example.com", "tooltip": "示例链接" }
        },
        "images": {
          "image_1": { ... }
        },
        "luckysheet_conditionformat_save": [
          {
            "type": "default",
            "cellrange": [{ "row": [0, 10], "column": [0, 5] }],
            "format": { "fontColor": "red" }
          }
        ],
        "chart": [
          {
            "chart_id": "chart_abc123",
            "width": 400,
            "height": 300,
            "left": 100,
            "top": 50,
            "option": { ... }
          }
        ]
      }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `sheets[].config` | object | Luckysheet 完整配置（包含 merge / columnlen / rowlen） |
| `sheets[].mergeConfig` | object | 合并单元格配置，键为 `"{row}_{col}"` |
| `sheets[].columnLen` | object | 列宽配置，键为列索引（数字字符串），值为像素宽度 |
| `sheets[].rowLen` | object | 行高配置，键为行索引（数字字符串），值为像素高度 |
| `sheets[].hyperlink` | object | 超链接配置，键为 `"{row}_{col}"` |
| `sheets[].images` | object | 图片配置 |
| `sheets[].luckysheet_conditionformat_save` | array | 条件格式配置 |
| `sheets[].chart` | array | 图表配置（ECharts 格式） |

#### 错误响应（404）

```json
{
  "code": 404,
  "success": false,
  "message": "文档不存在",
  "data": null
}
```

---

### 3.4 加载全部单元格数据 `GET /api/excel/document/{id}/sheet/{sheetId}/all`

加载指定 Sheet 的**全部单元格数据**（合并所有分块后返回）。

适用范围：前端打开文档后一次性拉取整个 Sheet 的单元格数据。
> 对于超大 Sheet（10 万行以上），建议评估性能后使用。

#### 请求

```
GET /api/excel/document/{id}/sheet/{sheetId}/all
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 文档 ID |
| `sheetId` | long | 是 | Sheet ID |

#### 成功响应（200）

```json
{
  "code": 200,
  "success": true,
  "message": "success",
  "data": {
    "sheetId": 1,
    "celldata": [
      {
        "r": 0,
        "c": 0,
        "v": {
          "v": "Hello",
          "m": "Hello",
          "ct": {
            "fa": "General",
            "t": "s"
          }
        }
      },
      {
        "r": 0,
        "c": 1,
        "v": {
          "v": 123.45,
          "m": "123.45",
          "ct": {
            "fa": "#,##0.00",
            "t": "n"
          }
        }
      },
      {
        "r": 1,
        "c": 0,
        "v": {
          "v": "=SUM(A1:B1)",
          "m": "公式",
          "ct": {
            "fa": "General",
            "t": "f"
          }
        }
      }
    ],
    "cellCount": 3
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `sheetId` | long | Sheet ID |
| `celldata` | array | Luckysheet 兼容的单元格数据数组 |
| `cellCount` | int | 单元格总数（非空单元格） |

**单元格值（`v` 对象）字段说明：**

| 子字段 | 类型 | 说明 |
|--------|------|------|
| `v` | mixed | 原始值（字符串/数字/布尔值） |
| `m` | string | 显示值（计算后的字符串形式） |
| `ct.fa` | string | 单元格格式字符串，如 `"General"`, `"#,##0.00"`, `"YYYY-MM-DD"` |
| `ct.t` | string | 单元格类型：`s`=文本, `n`=数字, `b`=布尔, `f`=公式, `e`=错误 |
| `bl` | int | 是否加粗（可选） |
| `fs` | int | 字号（可选） |
| `cl` | string | 字体颜色（可选） |
| `bg` | string | 背景色（可选） |
| `ht` | int | 水平对齐：0=居中, 1=左对齐, 2=右对齐（可选） |
| `vt` | int | 垂直对齐：0=居中, 1=上对齐, 2=下对齐（可选） |
| `tb` | string | 文本换行模式（可选） |
| `ps` | int | 内边距（可选） |
| `ff` | string | 字体名称（可选） |
| `fc` | string | 字体颜色代码（可选） |
| `un` | int | 下划线（可选） |
| `rt` | array | 富文本（可选） |

---

### 3.5 批量增量更新单元格 `PUT /api/excel/document/{id}/cells/batch`

将脏单元格按 **Chunk 分组**后逐块回写，**只影响相关分块**。

前端保存时，如果**只有单元格内容变化**（无结构/图片/图表变更），优先走此接口，避免全量重建。

#### 请求

```
PUT /api/excel/document/{id}/cells/batch
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 文档 ID（路径参数） |
| (body) | array | 是 | 单元格修改列表 |

**请求体：**

```json
[
  {
    "sheetId": 1,
    "r": 0,
    "c": 1,
    "v": {
      "v": "新值",
      "m": "新值",
      "ct": { "fa": "General", "t": "s" }
    }
  },
  {
    "sheetId": 1,
    "r": 2,
    "c": 3,
    "v": {
      "v": 100,
      "m": "100",
      "ct": { "fa": "#,##0", "t": "n" }
    }
  }
]
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `sheetId` | int/long | 是 | Sheet ID（对应 Java 实体 `ExcelSheet.id`） |
| `r` | int | 是 | 行号（0 起始） |
| `c` | int | 是 | 列号（0 起始） |
| `v` | object | 是 | 单元格值对象（Luckysheet 格式，见 3.4 节） |

> **删除单元格**：`v` 传 `null` 或空对象时会从 chunk 中移除该单元格条目。

#### 成功响应（200）

```json
{
  "code": 200,
  "success": true,
  "message": "保存成功",
  "data": null
}
```

当请求体为空列表时：

```json
{
  "code": 200,
  "success": true,
  "message": "没有需要保存的修改",
  "data": null
}
```

#### 错误响应（500）

```json
{
  "code": 500,
  "success": false,
  "message": "保存失败: xxx",
  "data": null
}
```

---

### 3.6 全量快照保存工作簿 `PUT /api/excel/document/{id}/workbook`

**全量替换**文档下所有 Sheet 和 Chunk（事务保护），即删除旧数据 → 重建新数据。

适用场景（当以下任一发生变化时）：
- Sheet 结构变化（增删 Sheet、改名、排序）
- 合并单元格、列宽、行高变更
- 图片/图表/超链接/条件格式变更

#### 请求

```
PUT /api/excel/document/{id}/workbook
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 文档 ID（路径参数） |
| (body) | object | 是 | 工作簿全量快照 |

**请求体：**

```json
{
  "sheets": [
    {
      "sheetIndex": 0,
      "sheetName": "Sheet1",
      "status": 1,
      "order": 0,
      "index": 0,
      "active": 1,
      "celldata": [
        { "r": 0, "c": 0, "v": { "v": "Hello", "m": "Hello", "ct": { "fa": "General", "t": "s" } } }
      ],
      "config": {
        "merge": {},
        "columnlen": { "0": 120 },
        "rowlen": {}
      },
      "mergeConfig": {},
      "columnLen": { "0": 120 },
      "rowLen": {},
      "hyperlink": {},
      "images": {},
      "luckysheet_conditionformat_save": [],
      "chart": []
    }
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `sheets` | array | 是 | Sheet 快照数组 |
| sheets[].`sheetIndex` | int | 是 | Sheet 序号（0 起始） |
| sheets[].`sheetName` | string | 是 | Sheet 名称 |
| sheets[].`status` | int | 是 | 状态（1=正常） |
| sheets[].`order` | int | 否 | 排序（同 sheetIndex） |
| sheets[].`index` | int | 否 | 索引（同 sheetIndex） |
| sheets[].`active` | int | 否 | 是否活动 Sheet（1=是） |
| sheets[].`celldata` | array | 否 | 单元格数据数组，也可使用 `data` 矩阵格式 |
| sheets[].`data` | array | 否 | 二维矩阵格式数据（替代 `celldata`） |
| sheets[].`config` | object | 是 | Luckysheet 配置（merge/columnlen/rowlen） |
| sheets[].`mergeConfig` | object | 否 | 合并单元格配置 |
| sheets[].`columnLen` | object | 否 | 列宽配置 |
| sheets[].`rowLen` | object | 否 | 行高配置 |
| sheets[].`hyperlink` | object | 否 | 超链接配置 |
| sheets[].`images` | object | 否 | 图片配置 |
| sheets[].`luckysheet_conditionformat_save` | array | 否 | 条件格式配置 |
| sheets[].`chart` | array | 否 | 图表配置 |

> **数据格式说明**：请求中的 `celldata` 和 `data` 字段二选一。`celldata` 为扁平数组（`{r, c, v}` 格式），`data` 为二维矩阵（`data[r][c]` 格式），优先使用 `celldata`。

#### 成功响应（200）

```json
{
  "code": 200,
  "success": true,
  "message": "保存成功",
  "data": {
    "sheetCount": 1,
    "sheetIdMap": {
      "0": 3,
      "1": 4
    }
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `sheetCount` | int | Sheet 数量 |
| `sheetIdMap` | object | Sheet 序号 → 新 Sheet ID 映射表，键为原始 `sheetIndex`（字符串），值为新生成的数据库 Sheet ID（long）。前端后续增量保存需使用此映射转换 sheetId |

#### 错误响应（500）

```json
{
  "code": 500,
  "success": false,
  "message": "保存失败: sheets 不能为空",
  "data": null
}
```

---

### 3.7 重命名文档 `PUT /api/excel/document/{id}/name`

更新文档名称。

#### 请求

```
PUT /api/excel/document/{id}/name
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 文档 ID（路径参数） |
| (body) | object | 是 | 请求体 |

**请求体：**

```json
{
  "name": "新的文档名称"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 新名称，不能为空 |

#### 成功响应（200）

```json
{
  "code": 200,
  "success": true,
  "message": "重命名成功",
  "data": null
}
```

#### 错误响应

```json
// 404 — 文档不存在
{ "code": 404, "success": false, "message": "文档不存在", "data": null }

// 500 — 名称为空
{ "code": 500, "success": false, "message": "名称不能为空", "data": null }
```

---

### 3.8 删除文档 `DELETE /api/excel/document/{id}`

软删除文档及其关联的 Sheet 和 Chunk 数据，并删除磁盘上的原始文件。

- 文档主记录：状态设置为 `3`（已删除）
- Sheet 记录：状态设置为 `3`（已删除）
- Chunk 记录：**物理删除**
- 原始文件：从磁盘删除

#### 请求

```
DELETE /api/excel/document/{id}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 是 | 文档 ID（路径参数） |

#### 成功响应（200）

```json
{
  "code": 200,
  "success": true,
  "message": "删除成功",
  "data": null
}
```

---

### 3.9 健康检查 `GET /`（仅 Python 后端）

Python 后端独有的根路径健康检查接口。

#### 请求

```
GET /
```

#### 成功响应（200）

```json
{
  "name": "DataLoom Python REST Backend",
  "status": "Running",
  "engine": "FastAPI"
}
```

---

## 4. 数据表结构

### 4.1 `excel_document` — 文档主表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT (PK, AUTO_INCREMENT) | 主键 |
| `name` | VARCHAR(255) | 文档名称 |
| `sheet_count` | INT | Sheet 数量 |
| `sheet_names` | VARCHAR(2000) | Sheet 名称 JSON 数组字符串，如 `["Sheet1","Sheet2"]` |
| `version` | BIGINT | 乐观锁版本号（初始 1） |
| `status` | INT | 状态：1=正常, 2=回收站, 3=已删除 |
| `file_path` | VARCHAR(500) | 原始文件本地路径 |
| `file_size` | BIGINT | 原始文件大小（字节） |
| `creator_id` | VARCHAR(64) | 创建者，默认 `"demo-user"` |
| `create_time` | DATETIME | 创建时间 |
| `update_time` | DATETIME | 更新时间 |

### 4.2 `excel_sheet` — Sheet 元数据表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT (PK, AUTO_INCREMENT) | 主键 |
| `document_id` | BIGINT | 所属文档 ID（外键 → excel_document.id） |
| `sheet_index` | INT | Sheet 序号（0 起始） |
| `sheet_name` | VARCHAR(255) | Sheet 名称 |
| `total_rows` | INT | 总行数 |
| `total_cols` | INT | 总列数 |
| `chunk_count` | INT | 分块数量 |
| `merge_config_json` | TEXT/CLOB | 合并单元格配置 JSON |
| `column_len_json` | TEXT/CLOB | 列宽配置 JSON |
| `row_len_json` | TEXT/CLOB | 行高配置 JSON |
| `config_json` | TEXT/CLOB | Luckysheet 完整 config JSON |
| `hyperlink_config_json` | TEXT/CLOB | 超链接配置 JSON |
| `images_config_json` | TEXT/CLOB | 图片配置 JSON |
| `condition_format_json` | TEXT/CLOB | 条件格式配置 JSON |
| `chart_json` | TEXT/CLOB | 图表配置 JSON |
| `active` | INT | 是否活动 Sheet：1=是, 0=否 |
| `status` | INT | 状态：1=正常, 3=已删除 |
| `create_time` | DATETIME | 创建时间 |
| `update_time` | DATETIME | 更新时间 |

### 4.3 `excel_sheet_chunk` — 单元格数据分块表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT (PK, AUTO_INCREMENT) | 主键 |
| `document_id` | BIGINT | 所属文档 ID（冗余，方便按文档批量删除） |
| `sheet_id` | BIGINT | 所属 Sheet ID（外键 → excel_sheet.id） |
| `chunk_index` | INT | 块序号（0 起始） |
| `row_start` | INT | 该块起始行号（含，0 起始） |
| `row_end` | INT | 该块结束行号（含，0 起始） |
| `celldata_json` | TEXT/CLOB | 单元格数据 JSON 数组，Luckysheet 兼容格式 |
| `create_time` | DATETIME | 创建时间 |

> **分块策略**：每 **1000 行**一个分块（`CHUNK_SIZE = 1000`），每个分块包含该行范围内所有非空单元格数据。

---

## 5. 附录

### 5.1 后端对比

| 维度 | Java 后端 | Python 后端 |
|------|-----------|-------------|
| 框架 | Spring Boot 2.1.15 | FastAPI 0.110+ / uvicorn |
| 数据库 | MySQL / H2（MySQL 模式） | SQLite（文件`./data/excel-demo.db`） |
| ORM | MyBatis-Plus 3.3.2 | SQLAlchemy 2.0+ |
| Excel 解析 | Apache POI 4.1.2 + EasyExcel 2.2.11 | openpyxl 3.1.2（双加载：公式+值） |
| 端口 | 9191 | 9191 |
| API 文档 | 无内置 | Swagger UI (`/docs`) + ReDoc (`/redoc`) |

> **API 完全兼容**：两个后端实现相同 REST 接口，响应格式一致。

### 5.2 保存策略说明

前端（`SheetEditor.vue`）采用**两种保存策略**：

| 策略 | 接口 | 触发条件 |
|------|------|----------|
| **增量保存** | `PUT /cells/batch` | **仅**单元格值发生变化 |
| **全量保存** | `PUT /workbook` | Sheet 结构/名称/顺序/配置/合并/列宽/行高/图片/图表/超链接/条件格式 任一变化 |

- 前端通过维护 `dirtyCells` 映射追踪值变更
- 通过对比初始化时的"结构快照"检测结构变更
- 全量保存后返回 `sheetIdMap`，前端需更新本地 sheetId 映射以保证后续增量保存正确

### 5.3 导出方案

**导出功能由前端实现**：使用 **ExcelJS** 在浏览器端从 Luckysheet 内部数据格式生成 `.xlsx` 文件，保证字体、颜色、边框、对齐、合并单元格、行列尺寸等样式零丢失。后端不提供导出接口。

### 5.4 部署配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 服务器端口 | 9191 | 后端端口 |
| 上传路径 | `./upload` | 上传文件的存储目录 |
| 上传限制 | 100MB | 单文件最大大小 |
| API 代理 | `/api/*` → `http://localhost:9191` | Nginx 反向代理（Docker 部署） |
| 前端端口 | 8081 | 开发环境 |

### 5.5 数据校验说明

- **分页参数**：`pageNum` 和 `pageSize` 为非负整数，默认分别为 1 和 20
- **文档名称**：重命名时名称不能为空或纯空白字符
- **工作表数据**：批量更新时请求体为空列表返回提示而非报错
- **Sheet 快照**：全量保存时 `sheets` 数组不能为空
- **文档删除**：重复删除或删除不存在的文档会正常返回成功（幂等设计）

---

> © 2026 DataLoom. 本文档对应 Java 后端 `dataloom-server` 和 Python 后端 `dataloom-server-python`。
