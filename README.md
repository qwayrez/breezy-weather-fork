<div align="center">
<br />
<img src="app/src/res_breezy/mipmap-xxxhdpi/ic_launcher_round.webp" alt="Logo" />
</div>

<h1 align="center">JX Weather</h1>

<br />

<div align="center">
  <img alt="API 21+" src="https://img.shields.io/badge/Api%2021+-50f270?logo=android&logoColor=black&style=for-the-badge" />
  <a href="https://kotlinlang.org/">
    <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-a503fc?logo=kotlin&logoColor=white&style=for-the-badge" />
  </a>
  <a href="https://developer.android.com/compose">
    <img alt="Jetpack Compose" src="https://img.shields.io/static/v1?style=for-the-badge&message=Jetpack+Compose&color=4285F4&logo=Jetpack+Compose&logoColor=FFFFFF&label=" />
  </a>
  <a href="https://m3.material.io/">
    <img alt="Material 3 Expressive" src="https://custom-icon-badges.demolab.com/badge/m3%20expressive-lightblue?style=for-the-badge&logoColor=333&logo=material-you" />
  </a>
</div>


<h4 align="center">JX Weather 是一个基于 Breezy Weather 的个人 fork，主要目的是加入和风天气（QWeather）数据源。未来可能会根据自身需求新增一些功能。</h4>

<div align="center">
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/01-main-header-light.png" alt="" style="width: 300px" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/02-main-header-dark.png" alt="" style="width: 300px" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/03-main-blocks-1.png" alt="" style="width: 300px" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/04-main-blocks-2.png" alt="" style="width: 300px" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/05-settings.png" alt="" style="width: 300px" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/06-sources.png" alt="" style="width: 300px" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/07-details.png" alt="" style="width: 300px" />
</div>

# ✨ 主要特性

- 🌤️ 天气数据
  - 最多 16 天的逐日与逐小时预报
  - 未来一小时降水预报
  - 恶劣天气与降水预警
  - 温度 / 体感温度 / 常年同期值
  - 降水
  - 风力
  - 空气质量
  - 花粉与霉菌
  - 湿度
  - 紫外线指数
  - 能见度
  - 气压
  - 日出日落
  - 月相
- 📊 可视化
  - 详细的 24 小时图表
  - Material 3 Expressive 卡片
- 🏭 支持众多天气源，包括新增的和风天气（QWeather）
- 🧩 多种桌面小部件
- 🖼️ 动态壁纸
- 📦 自定义图标包
- 🌙 自动深色模式
- 🔁 可选的数据共享（如 Gadgetbridge）

# 🛟 帮助

* [常见问题 / 帮助](HELP.md)
* [主屏幕说明](docs/HOMEPAGE.md)
* [天气源对比](docs/SOURCES.md)

# 🤝 贡献

本 fork 主要为个人维护。如果你有兴趣，欢迎提交 Pull Request 或 issue 讨论。

* [贡献指南（含新增天气源说明）](CONTRIBUTE.md)

# 🌍 翻译

翻译工作继承自上游 Breezy Weather。如需参与上游翻译，请访问 [Weblate](https://hosted.weblate.org/projects/breezy-weather/breezy-weather-android/#information)。

# 📜 许可

* [GNU Lesser General Public License v3.0](/LICENSE)
* 本许可证不授予任何贡献者的商标、服务标志或徽标权利。
* 禁止对材料来源进行虚假陈述，修改后的版本必须以合理方式标明与原始版本不同。

本 fork 基于 [Breezy Weather](https://github.com/breezy-weather/breezy-weather) 构建。若计划分发本 fork 的构建版本，请注意：

- 遵守项目的 LICENSE
- 避免与原版 Breezy Weather 混淆：
  - 分发时请勿使用 `breezy` flavor
  - 修改 [`res_fork/values/strings.xml`](app/src/res_fork/values/strings.xml) 中的应用名称
  - 更换 [`res_fork`](app/src/res_fork) 中的应用图标
  - 避免安装冲突：修改 [`app/build.gradle.kts`](app/build.gradle.kts) 中的 `applicationId`
