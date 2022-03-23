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
