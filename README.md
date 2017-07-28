> 作者：林冠宏 / 指尖下的幽灵

> 掘金：https://juejin.im/user/587f0dfe128fe100570ce2d8

> 博客：http://www.cnblogs.com/linguanh/

> GitHub ： https://github.com/af913337456/

---

### 效果展示
</br>
<img src="https://user-gold-cdn.xitu.io/2017/7/28/139a9a1835fa730c4d3a1b1a9b3d0088" width = "350" height = "650" alt="下雨"/>
<img src="https://user-gold-cdn.xitu.io/2017/7/28/17afc1d33348f8ba19dbcdabf812d269" width = "350" height = "650" alt="下雨"/>
<img src="https://user-gold-cdn.xitu.io/2017/7/28/e4871c774cbde073ea06015aff7fa3ec" width = "350" height = "650" alt="下雨"/>
<img src="https://user-gold-cdn.xitu.io/2017/7/28/aa2994532547a0a0df109dfc30cecdd7" width = "350" height = "650" alt="下雨"/>

### GitHub 链接
本着开源的精神，如果对你有用，希望您能给予个星星(star)
https://github.com/af913337456/WeChatVideoView

### 功能点

1，<strong>直观的</strong>
* 按键触发录音
* 上移动或者其它移动可以撤销
* 动态根据声音分贝显示图片进度效果
* 录音时间过短的提示

2，<strong>隐藏的</strong>
* 0 耦合，dialog 在内的 UI 代码我也集合了，业界风格。
* 使用方便，下面见解析
* 低内存占用，提供捞底回收
* 多接口延伸，例如结果文件路径的返回，和文件的大小
* 基于 Speex jni 库，高效，自定义语音文件格式

### 解析
实例:
```java
private VoiceRecorderHelper voiceRecorderHelper;
```
触发：
```java
// R.id.record 这个 VIew 是你的触发 View
findViewById(R.id.record).setOnTouchListener(
        new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:  /** 手指按下 */
                        voiceRecorderHelper.Action_Down(v,motionEvent);
                        return true;
                    case MotionEvent.ACTION_MOVE:  /** 移动 */
                        voiceRecorderHelper.Action_Move(v,motionEvent);
                        return true;
                    case MotionEvent.ACTION_UP:    /** 抬起 */
                        voiceRecorderHelper.Action_Up(v,motionEvent);
                        return true;
                }
                return false;
            }
        }
);
```
初始化：
```java
voiceRecorderHelper =
                new VoiceRecorderHelper(
                        this,
                        new VoiceRecorderHelper.CallBack()
                {
                        @Override
                        public String setOutPutPath() {
                            /** 设置录音结果路径，你的格式也在这里设置 */
                            return getAudioSavePath("LinGuanHong");
                        }

                        @Override
                        public void onDown(View v) {
                            /** 纯粹的 down 事件回调 */
                        }

                        @Override
                        public void onMove_in_limit(View v) {
                            /** 手指移动的范围在限制内 */
                        }

                        @Override
                        public void onMove_out_limit(View v) {
                            /** 手指移动超过范围，内部做了显示取消的提示 */
                        }

                        @Override
                        public void onUp_start(View v) {
                            /** 纯粹的 Up 事件回调 */
                        }

                        @Override
                        public void onUp_cancel(View v) {
                            /** 这个时候已经因为手指移动超过范围取消了录音 */
                        }

                        @Override
                        public void onFinishRecord() {
                            /** 录音结束 */
                        }

                        @Override
                        public void onRecordSuccess(float len, String savePath) {
                            /** 录音、解码、保存成功 */
                            Log.e("aaaaa", "录音的路径 " + savePath + " 长度 " + len);
                        }

                        @Override
                        public void onRecordVolumeChange(int voiceValue) {
                            /** 录音声音强度的变化，单位分贝 */
                        }
                    }
                );
    }
```

### 技术点 (可以不看)
jni层：编译好 Speex.so 库，使用提供的录制函数。
java层：异步线程录制，获取分贝等数据同步刷新UI
