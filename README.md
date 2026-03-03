
中文（繁體） | [English](/README-en.md)

**這是一 個人倉庫，不保證後續維護與是否穩定**

---

OticLocal 是一款能讓安卓設備轉發自身麥克風輸入至本地環回網路的小工具

OticLocal 並不負責接收音訊信號所以需要編寫自訂腳本來接收

其中一種辦法就是透過nc來接收音訊至虛擬麥克風到您安卓設備上的本地容器的PulseAudio

此為一個接收腳本示例：
```bash
#!/bin/bash

VIRTUAL_MIC_SINK="OticLocalMic_Sink"
VIRTUAL_MIC_SOURCE="OticLocalMic"
PHONE_IP="127.0.0.1"
PORT="<FIND_PORT_FROM_OTIC>"

SINK_MODULE_ID=""
SOURCE_MODULE_ID=""

cleanup() {
    if [ -n "$SOURCE_MODULE_ID" ]; then
        pactl unload-module "$SOURCE_MODULE_ID" 2>/dev/null
    fi
    if [ -n "$SINK_MODULE_ID" ]; then
        pactl unload-module "$SINK_MODULE_ID" 2>/dev/null
    fi
    exit 0
}

trap cleanup EXIT INT TERM

SINK_MODULE_ID=$(pactl load-module module-null-sink \
    sink_name="$VIRTUAL_MIC_SINK" \
    sink_properties=device.description="OticLocal_Receiver_Sink")

if [ -z "$SINK_MODULE_ID" ]; then
    echo "Failed to create virtual sink"
    exit 1
fi

SOURCE_MODULE_ID=$(pactl load-module module-remap-source \
    master="$VIRTUAL_MIC_SINK.monitor" \
    source_name="$VIRTUAL_MIC_SOURCE" \
    source_properties=device.description="OticLocal_Receiver")

if [ -z "$SOURCE_MODULE_ID" ]; then
    echo "Failed to create virtual microphone"
    exit 1
fi

echo "Virtual microphone ready"
echo "Connecting to $PHONE_IP:$PORT"


nc -w 3 "$PHONE_IP" "$PORT" | pacat --playback \
    --device="$VIRTUAL_MIC_SINK" \
    --format=s16le \
    --rate=48000 \
    --channels=1 \
    --latency-msec=10

echo "Stream stopped"
```

註： `sample-rate`與`format`必須為`48000`與`s16le`(`pcm`)否則很可能不能正常工作。此外，腳本示例中的`<FIND_PORT_FROM_OTIC>`需要按照實際設定的端口進行更改，預設為58585。

記得使用時要先啟動OticLocal的音訊串流再跑接收腳本，同時此腳本可以按需求配合`nohup`使用


------

在此有一段個人使用於Termux上應用範例，不保證其為最佳的實現方法與不會出錯，可以按照自身需求進行更改。


此示例依賴於`jq nc pulseaudio termux-api termux-am`，請在操作前先準備好環境。



1.先`mkdir "${PREFIX}/my_script"`建立自訂腳本資料夾

2.然後在`~/.bashrc`上適合的位置（通常是尾部）加上`export PATH="${PATH}:${PREFIX}/my_script"`讓其可以方便呼叫

3.然後`cd "${PREFIX}/my_script"`進入剛建立的資料夾

4.建立一個`opmic`並填入以下內容
```bash
#!/bin/bash
am start --user 0 com.zj978.oticlocal/.OticLocalActivity
while true; do
        echo 'push the start button'
        result=$(termux-notification-list | jq '.[] | select(.packageName=="com.zj978.oticlocal") | {title, content, when}')
        if [ "$result" != "" ]; then
                break
        fi
        sleep 0.3
done
echo "oticlocal server is ready"
```
（註：`am start`與`termux-notification-list`在termux 上偶爾可能失效，如果日常常駐使用Shizuku並安裝了[termux-shizuku-tools](https://github.com/AlexeiCrystal/termux-shizuku-tools) ，那麼可以改用`shizuku e 'am start -n com.zj978.oticlocal/.OticLocalActivity'`，可能提升體驗）

5.建立一個`oticlocal`並填入上方說明處的接收示例腳本(**記得更改端口的佔位文本**)

6.然後`chmod +x oticlocal && chmod +x opmic`給予執行權限

7.建立一個桌面啟動器或一個`alias`別名於`~/.bashrc`並讓其可以方便執行`bash -lic "${PREFIX}/my_script/opmic && ${PREFIX}/my_script/oticlocal"`

8.最後`source ~/.bashrc`讓在`~/.bashrc`的更改被套用




## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=gh-zj978/OticLocal&type=date&legend=top-left)](https://www.star-history.com/#gh-zj978/OticLocal&type=date&legend=top-left)