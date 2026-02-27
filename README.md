**This is a personal fork for self-use. Not guaranteed to be stable or maintained.**

---

OticLocal lets you stream your Android device's microphone input to the local loopback using
`ServerSocket` and Foreground Service.

OticLocal isn't a receiver, so you must have a custom receiver to receive the audio from the OticLocal client.

The easiest way to do this is to create a virtual microphone using PipeWire/PulseAudio on your Linux
machine and source the audio via GStreamer:

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

Note that `sample-rate` must be `48000` and the `format` must be `s16le` (`pcm`), as these are the
exact values used by the app.  `<FIND_PORT_FROM_OTIC>` should also be replaced with valid values.

Always start streaming from the app first, and then run the script. You might also want to use
`nohup` since the stream terminates when you exit the script.

------

Here is a personal application scenario using Termux for reference only. It cannot be guaranteed to be the best implementation or free from errors. Please adjust it according to your individual needs.

This demo depends on `jq nc pulseaudio termux-api termux-am`. Please configure it properly before using it.



1.`mkdir "${PREFIX}/my_script"`

2. Add `export PATH="${PATH}:${PREFIX}/my_script"` at `~/.bashrc`

3.`cd "${PREFIX}/my_script"`

4.Establish a file named `opmic` containing 
```bash
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
(Note: `am start` and `termux-notification-list` on Termux may sometimes fail to work.)

5.Establish a file named `oticlocal` containing the demo above (**Please remember to fill the port before execute it**)

6.`chmod +x oticlocal && chmod +x opmic`

7.Add a shortcut at the desktop or `alias` in `~/.bashrc` with command `bash -lic "${PREFIX}/my_script/opmic && ${PREFIX}/my_script/oticlocal"`