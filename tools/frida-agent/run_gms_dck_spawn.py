import time
import frida

TARGET = "com.google.android.gms"
BASE = r"c:/Users/anderson/Projetos/UnlockXiaomi/tools/frida-agent"

def main():
    src = open(BASE + "/_agent.js", encoding="utf-8").read() + "\n" + \
          open(BASE + "/gms-dck-unlock.js", encoding="utf-8").read()
    dev = frida.get_usb_device(timeout=10)
    pid = dev.spawn([TARGET])
    session = dev.attach(pid)

    def on_msg(msg, data):
        if msg.get("type") == "send":
            print("[SCRIPT]", msg.get("payload"), flush=True)
        elif msg.get("type") == "error":
            print("[ERR]", msg.get("description"), flush=True)

    script = session.create_script(src)
    script.on("message", on_msg)
    script.load()
    dev.resume(pid)
    print("[*] spawned+hooked", TARGET, "pid", pid, flush=True)

    def on_detached(reason, crash):
        print("[!] DETACHED:", reason, crash, flush=True)
    session.on("detached", on_detached)

    while True:
        time.sleep(3600)

if __name__ == "__main__":
    main()
