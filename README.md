# Event Delete 支持预处理功能

## 1. 概述

* 功能简介: event_delete 在删除数据时, 支持预处理功能, 允许客户自定义删除逻辑
* 版本要求: sdf >= 2.3.1.33
* 目前仅客户 「丽兹行」 需要使用, 如果其它客户也需要使用该功能, 可以参考该教程自行安装预处理功能

## 2.预处理开发方法

### 预处理模块仅需定义一个接口, 并自行实现该接口即可, 下面是接口示例:

EventDeletePreprocessor.java

```java
package com.sensorsdata.platform.dataloader.worker.processor;

/**
 * @author wuyongkang@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/03/08 16:29
 */
public interface EventDeletePreprocessor {

  /**
   * 客户自定义删除逻辑
   * @param eventData 一条 Json, String 类型的 event 数据
   * Json 格式样例
   * {
   *   "bucket_id": 0,
   *   "day_id": 18830,
   *   "distinct_id": "test_distinct_id",
   *   "dtk": null,
   *   "event": "TestEventName",            // 事件名
   *   "mutable_event_key_property": null,
   *   "offset": 612,
   *   "project_id": 1,
   *   "properties": {                      // 事件的所有属性
   *     "$ip": "10.90.28.102",
   *     "$is_login_id": false,
   *     "$lib": "Java",
   *     "$lib_version": "3.1.10",
   *     "bool1": true,
   *     "list2": [
   *       "element1"
   *     ],
   *     "num3": 1234,
   *     "str4": "0512"
   *   },
   *   "real_timestamp": 1629686984720,
   *   "receive_timestamp": 1629686984720,
   *   "type": "track",
   *   "update_mutable_event": false,
   *   "user_id": 74031585797665980,
   *   "ver": 0
   * }
   * 详细格式可见文档介绍: https://manual.sensorsdata.cn/sa/latest/%E6%95%B0%E6%8D%AE%E6%A0%BC%E5%BC%8F-1573774.html#id-.%E6%95%B0%E6%8D%AE%E6%A0%BC%E5%BC%8Fv1.13-%E9%99%90%E5%88%B6
   *
   * @return true: 删除这条数据, false: 保留这条数据
   */
  boolean process(String eventData);
}

```

* 该接口类需要原封不动的挪到自己的预处理项目中 (即包名, 接口名, 接口方法入参 / 返回值等）都需与模板中定义好的接口类一致
* 接口是不允许向上层抛出异常的, 如果在删除数据过程中, 预处理模块抛出了运行时异常, 则会终止整个删除任务
* 每一条数据在删除前都会传到该接口中, 并通过返回值判断是否要进行删除
* 入参: 一条 JSON 格式的 Event 数据; 
* 返回值: 布尔值. true: 删除该数据 / false: 保留该数据

### 实现了该接口的预处理代码示例:

EventDeletePreprocessorDemo.java

```java
package com.sensorsdata.platform.dataloader.worker.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * @author wuyongkang@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/03/09 17:49
 */
@Slf4j
public class EventDeletePreprocessorDemo implements EventDeletePreprocessor{
  private ObjectMapper objectMapper = new ObjectMapper();
  /**
   * 样例数据
   * {
   * "bucket_id": 0,
   * "day_id": 18830,
   * "distinct_id": "test_distinct_id",
   * "dtk": null,
   * "event": "TestEventName",            // 事件名
   * "mutable_event_key_property": null,
   * "offset": 612,
   * "project_id": 1,
   * "properties": {                      // 事件的所有属性
   * "$ip": "10.90.28.102",
   * "$is_login_id": false,
   * "$lib": "Java",
   * "$lib_version": "3.1.10",
   * "bool1": true,
   * "list2": [
   * "element1"
   * ],
   * "num3": 1234,
   * "str4": "0512"
   * },
   * "real_timestamp": 1629686984720,
   * "receive_timestamp": 1629686984720,
   * "type": "track",
   * "update_mutable_event": false,
   * "user_id": 74031585797665980,
   * "ver": 0
   * }
   **/
  @Override
  public boolean process(String eventData) {
    try {
      // 1. 将 string 类型的 JSON 数据 EventData 解析成 Map<String, Object>
      Map<String, Object> eventDataMap = objectMapper.readValue(eventData, Map.class);
      // 2. 获取到事件名
      Object eventName = eventDataMap.get("event");
      // 3. 获取到事件的所有属性
      Map<String, Object> properties = (Map<String, Object>) eventDataMap.get("properties");
      // 4. 获取事件中属性 "$is_login_id" 的值
      Object isLoginId = properties.get("$is_login_id");
      if ((eventName != null && "Test".equals(eventDataMap.get("event"))) ||
          (isLoginId != null && false == (Boolean) isLoginId)) {
        return true;
      }
    } catch (IOException e) {
      log.error("preprocessor error.");
      throw new RuntimeException(e);
    }
    return false;
  }
}
```

* 预处理代码示例中, 会删除所有 「事件名等于 'Test'」 或者 「事件的属性 $is_login_id 等于 false」的数据


## 3. 编译打包

我们需要将编写好的删除任务预处理模块打包安装到神策的环境中

该 demo 样例中使用了 Jackson 库解析 JSON，并使用 Maven 做包管理.

pom.xml 文件示例:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>delete-preprocessor-demo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.9.10</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.2</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.12</version>
        </dependency>
    </dependencies>

    <!-- 编写预处理功能时, 建议加上下面的 shade 打包插件, 这样可以避免自己预处理代码中引入的依赖 jar 包和神策环境中依赖的 jar 包版本冲突带来的一系列问题 -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.4.3</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <createDependencyReducedPom>false</createDependencyReducedPom>
                            <transformers>
                                <transformer
                                        implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.3</version>
                    <configuration>
                        <source>1.8</source>
                        <target>1.8</target>
                        <encoding>UTF-8</encoding>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```


编译并打包该 demo 代码可通过：

```bash
git clone https://github.com/2674891231/delete-preprocessor-demo.git
cd delete-preprocessor-demo
mvn clean package
```

执行编译后可在 `target` 目录下找到 `preprocessor-sample-1.0-SNAPSHOT.jar` 并上传至服务器