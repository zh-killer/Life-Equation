# 项目长期记忆 - life_equation_mini

## 项目概况
- 生命方程式微信小程序（uni-app + Vue3 `<script setup>`）
- 蓝本项目：`C:\Users\ZH\IdeaProjects\life_eqation`（Vue3 Web 版，功能权威参考）
- 后端 API：`http://localhost:8888/api`，统一返回 `{ code, data, message }`，token Bearer
- 后端：`C:\Users\ZH\IdeaProjects\Quation of Life`（Spring Boot 3.2.5），MySQL `life_equation`(root/1234) + Redis(可选,代码 fail-open)
- **后端连接 MySQL 端口 3306**（非 3308），表名 `user`（单数，无 deleted 列）
- mysql 客户端：`D:\software\DataSpace\bin\mysql.exe`
- 测试账号：`user1`/`123456`(患者) `doctor1-6`/`123456`(医生) `admin`/`123456`（均已通过注册 API 创建于 3306 端口库）
- 微信小程序 AppID：`wx5b6730e12c9f2469`（已填入 manifest.json 的 mp-weixin.appid 及 dist 产物）

## 目录结构
- `src/utils/request.js`：uni.request 封装（get/post/put/delete/upload）
- `src/utils/api.js`：所有接口函数
- `src/store/user.js`：reactive 全局用户状态
- `src/pages/patient/*`：患者端；`src/pages/doctor/*`：医生端；`src/pages/detail/*`：医生/医院详情
- `src/pages.json`：路由 + globalStyle（导航栏 #1A3C6E）

## 状态枚举
- 问答：PENDING/ANSWERED/CLOSED
- 预约：PENDING/CONFIRMED/COMPLETED/CANCELLED
- 角色：USER/DOCTOR/ADMIN

## 已知约定
- 日期选择用 `<picker mode="date">`
- 富文本用 `<rich-text :nodes>`
- 地图用 `<map>` + markers，有 lat/lng 时展示，否则 uni.openLocation 导航
- 文件上传用 `request.upload`（uni.uploadFile，name 默认 image）
- Element Plus 组件对应小程序原生替代（picker/radio-group/switch/swiper/map 等）

## 构建注意事项
- **vite.config.js 的 `emptyOutDir: false` 是隐患**：构建失败后残缺产物不会被清理，vendor.js 可能只剩注释头导致 App 白屏。建议删除此配置或构建前手动清 dist/build
- `src/utils/config.js` 用 `require('../static/env.js')` 混合 CommonJS/ESM，应改为 ESM import
- `pages.json` easycom 引用了不存在的 `uni_modules/uview-ui`，应清理
- `project.config.json` appid 为 `touristappid`（游客模式，wx.login 受限）
- 登录页 `<image src="*.svg">` 在小程序中不渲染（SVG 不支持）
- 构建后应检查 `dist/build/mp-weixin/common/vendor.js` 大小是否正常（~63KB）

## 待办/可选增强
- 管理端页面未移植（参考项目 admin/*，优先级低）
- 文章列表缺分类筛选、就诊指南缺 FAQ/搜索过滤
- AI 导诊历史详情/删除可细化
