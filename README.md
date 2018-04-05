# 项目简介
本项目用于简化android BLE的使用，解决了：
1.android手机适配
2.发送数据自动拆包，无需关注拆包问题
3.解决android BLE假连接问题
4.数据发送扁平化（把所有数据串行发送，避免资源竞争）


# 使用方法
1. 创建/获取蓝牙管理对象
    TBBLEManager tbbleManager = TBBLEManager.getInstance();
2. 扫描蓝牙
    tbbleManager.startScan(TBBLEScanMode scanMode, UUID[] uuids, TBBLEScanCB                                 scanCB, int workTime, int sleepTime, int workTimes)
    扫描蓝牙的方式（扫描workTime毫秒 -> 停止扫描sleepTime(毫秒)->扫描workTime毫秒 -> 停止扫描sleepTime(毫秒)……）这样循环workTimes次（-1时，为循环无限次）

3. 创建蓝牙连接对象
    TBBLEBase tbbleBase = new TBBLEBase(TBBLEManager.getInstance(), this);

4. 启动连接(连接完后，会自动发现全部服务，无需手动发现服务)
    tbbleBase.connect(TBBLEDevice device, int timeout, int retryTimes, TBBLEBaseCB cb);
    tbbleBase.connect(String mac, int timeout, int retryTimes, TBBLEBaseCB cb);
    timeout(毫秒)连接超时时间， retryTimes重试次数， cb 结果回调
5. 获取所有服务列表
    tbbleBase.getAllServices();
6. 发送数据(数据无20 bytes限制，请尽情嗨，具体参考CommunicateActivity.java)
    TBBLEWriteCharacterAction writeCharacterAction = new TBBLEWriteCharacterAction(
        gData.workingChararcter, sendData, new TBBLEBaseAction.TBBLEBaseOption(),
        writeCB);

    tbbleBase.addAction(writeCharacterAction);
7. 开启接收
    TBBLEEnNotifyAction notifyAction = new TBBLEEnNotifyAction(BluetoothGattCharacteristic characteristic, boolean enable, TBBLEBaseOption option, TBBLEBaseActionCB cb);

    tbbleBase.addAction(notifyAction);

8. 读取rssi，读取特征值(所有操作，都是添加任务的形式，只是任务对象不一样)
    tbbleBase.addAction(TBBLEReadRssiAction);  // 添加读取rssi任务
    tbbleBase.addAction(TBBLEReadCharacterAction);  // 添加读取特征值任务
