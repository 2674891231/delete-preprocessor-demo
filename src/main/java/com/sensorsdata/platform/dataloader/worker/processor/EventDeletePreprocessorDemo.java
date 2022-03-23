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
