import subprocess
import sys
import time

import frida

TARGET = 'app.morphe.android.apps.youtube.music'
SERIAL = '4d7fc9af'
ADB = 'tools/platform-tools/adb.exe'
HOOK = 'analysis/ytmusic-morphe/frida/mbs-hook.js'


def on_message(message, data):
    if message.get('type') == 'send':
        print(message.get('payload'), flush=True)
    else:
        print('[err]', message, flush=True)


def get_pid():
    try:
        out = subprocess.run(
            [ADB, '-s', SERIAL, 'shell', 'pidof', TARGET],
            capture_output=True, text=True, timeout=30,
        ).stdout.strip()
    except Exception:
        return None
    return int(out) if out.isdigit() else None


def main():
    device = frida.get_usb_device(timeout=15)
    print('device:', device, flush=True)
    attached_pid = None
    detached = {'flag': False}

    while True:
        pid = get_pid()
        if pid and pid != attached_pid:
            try:
                session = device.attach(pid)
                with open(HOOK, 'r', encoding='utf-8') as f:
                    src = f.read()
                script = session.create_script(src)
                script.on('message', on_message)

                def on_detached(reason, crash, p=pid):
                    print('[detached] pid %s: %s' % (p, reason), flush=True)
                    detached['flag'] = True

                session.on('detached', on_detached)
                script.load()
                attached_pid = pid
                detached['flag'] = False
                print('[attached] pid %s — hooks ativos' % pid, flush=True)
            except Exception as e:
                print('[attach fail] pid %s: %s' % (pid, e), flush=True)
                attached_pid = None
                time.sleep(1)
                continue
        if detached['flag']:
            attached_pid = None
            detached['flag'] = False
        time.sleep(0.5)


if __name__ == '__main__':
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        pass
