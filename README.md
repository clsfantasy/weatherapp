# WeatherApp

## 项目简介

WeatherApp 是一个基于 Kotlin 和 Android Studio 开发的天气查询应用。用户可以输入城市名，实时查询该城市的天气信息（温度、天气状况、更新时间），并自动缓存历史查询记录，支持离线查看。应用采用了 OkHttp + Gson 进行网络请求与数据解析，Room 数据库实现本地缓存，界面简洁，体验流畅。

---

## 主要功能

- **城市天气查询**：输入城市名，自动模糊匹配并查询天气。
- **天气信息展示**：显示温度、天气状况、更新时间等。
- **历史记录**：展示所有已查询过的城市及其天气信息，支持离线查看。
- **本地缓存**：查询过的数据本地保存，1小时内重复查询优先使用缓存。
- **网络状态检测**：查询前自动检测网络状态，无网络时友好提示。
- **网络请求重试与超时处理**：请求失败自动重试，超时有明确提示。

---

## 代码结构

```
WeatherApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/weatherapp/
│   │   │   │   ├── MainActivity.kt             // 主界面与业务流程
│   │   │   │   ├── WeatherApi.kt               // 网络请求与数据解析
│   │   │   │   ├── WeatherEntity.kt            // Room实体
│   │   │   │   ├── WeatherDao.kt               // Room DAO接口
│   │   │   │   ├── AppDatabase.kt              // Room数据库
│   │   │   │   ├── WeatherHistoryAdapter.kt    // 历史记录RecyclerView适配器
│   │   │   │   ├── WeatherInfo.kt              // 天气数据模型
│   │   │   │   ├── GeoResponse.kt              // 城市搜索数据模型
│   │   │   │   ├── NetUtil.kt                  // 网络状态检测工具
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml       // 主界面布局
│   │   │   │   │   ├── item_weather_history.xml// 历史记录item布局
│   │   │   │   ├── values/                     // 主题、字符串等
│   │   │   │   ├── drawable/                   // 图标资源
│   │   │   ├── AndroidManifest.xml
│   ├── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
```

---

## 代码亮点

- **网络请求与解析**：使用 OkHttp 处理 Gzip 压缩响应，Gson 自动映射 JSON 到数据类。
- **城市名模糊搜索**：通过 GeoAPI 实现城市名模糊匹配，支持中文和拼音。
- **Room 本地缓存**：天气数据本地持久化，历史查询一目了然。
- **网络检测与重试**：查询前自动检测网络，网络请求失败自动重试并有超时提示。
- **UI体验**：ConstraintLayout + RecyclerView，界面简洁，历史与实时天气分区明显。

---

## 技术难点与解决方案

1. **API KEY 安全性**  
   - 采用 Gradle buildConfigField 方式，将密钥放在本地配置文件，避免硬编码在源码中，提升安全性。
   - 正常来说该配置文件需要放在.gitignore中，但因为作业需要，所以上传到了仓库中。

2. **城市名模糊搜索与编码**  
   - 使用 URLEncoder 对中文城市名进行编码，保证 API 请求兼容性。

3. **网络请求超时与重试**  
   - OkHttp 设置合理超时时间，catch SocketTimeoutException 并自动重试，提升用户体验。

4. **本地缓存与数据一致性**  
   - Room 数据库自动管理缓存，查询逻辑优先本地，保证离线可用和数据新鲜度。

5. **网络状态检测**  
   - 使用 ConnectivityManager 检查网络状态，避免无网时无效请求和崩溃。

---

## 如何运行

1. 克隆项目到本地，使用 Android Studio 打开。
2. 在 `apikey.properties` 中配置你的 QWeather API KEY 和 HOST。
3. 同步 Gradle，连接模拟器或真机，点击运行即可体验。

---

## 依赖环境

- Android Studio Giraffe 以上
- Kotlin 2.0+
- OkHttp 4.12+
- Gson 2.10+
- Room 2.6+
- AndroidX 相关依赖

---

## 学习总结
1. 网络状态未检测导致无效请求或崩溃 
- 问题表现：
在无网络环境下直接发起请求，导致应用崩溃或用户体验差。 
- 优化思路：
在每次发起网络请求前，使用 ConnectivityManager 检查当前网络状态。若无网络，及时给出用户提示，避免无效请求和崩溃。   
2. 请求超时或服务器无响应导致界面卡死 
- 问题表现：
当服务器响应慢或网络不稳定时，OkHttp 默认超时时间可能过短或过长，导致请求长时间无响应，影响用户体验。 
- 优化思路：
为 OkHttpClient 设置合理的 connectTimeout、readTimeout，并在请求代码中捕获 SocketTimeoutException，给予用户“请求超时”提示。同时实现自动重试机制（如重试3次），提升请求成功率。   
3. API Key 等敏感信息硬编码，存在安全隐患 
- 问题表现：
API Key、Host 等敏感信息直接写在代码中，容易被反编译获取，存在安全风险。 
- 优化思路：
将敏感信息放在本地配置文件（如   apikey.properties），通过 Gradle 的 buildConfigField 注入到 BuildConfig，在代码中通过 BuildConfig 访问，避免硬编码和泄露。同时将配置文件加入   .gitignore，防止密钥被上传到代码仓库。   

   总结：
通过网络状态检测、超时与重试机制、敏感信息安全管理等优化措施，显著提升了应用的健壮性和安全性，改善了用户体验。