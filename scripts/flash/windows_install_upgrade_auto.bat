@echo off
cd %~dp0
set fastboot=bin\windows\fastboot.exe
if not exist %fastboot% echo %fastboot% not found. & pause & exit /B 1
echo Waiting for device...
set device=
for /f "tokens=2" %%A in ('%fastboot% getvar product 2^>^&1 ^| findstr "\<product:"') do set device=%%A
if "%device%" equ "" echo Your device could not be detected. & pause & exit /B 1
echo Your device: %device%
if "%device%" neq "popsicle" echo Compatible devices: popsicle & pause & exit /B 1

echo Your device will be flashed without formatting the data partition.
echo You will keep your apps, settings and files on internal storage.
echo ##############################################################
echo Please wait. The device will reboot once flashing is complete.
echo ##############################################################
%fastboot% set_active a
%fastboot% flash abl_ab images\abl.img
%fastboot% flash aop_ab images\aop.img
%fastboot% flash aop_config_ab images\aop_config.img
%fastboot% flash bluetooth_ab images\bluetooth.img
%fastboot% flash cpucp_ab images\cpucp.img
%fastboot% flash cpucp_dtb_ab images\cpucp_dtb.img
%fastboot% flash dcp_ab images\dcp.img
%fastboot% flash devcfg_ab images\devcfg.img
%fastboot% flash dsp_ab images\dsp.img
%fastboot% flash dtbo_ab images\dtbo.img
%fastboot% flash featenabler_ab images\featenabler.img
%fastboot% flash hyp_ab images\hyp.img
%fastboot% flash hyp_ac_config_ab images\hyp_ac_config.img
%fastboot% flash idmanager_ab images\idmanager.img
%fastboot% flash imagefv_ab images\imagefv.img
%fastboot% flash keymaster_ab images\keymaster.img
%fastboot% flash modem_ab images\modem.img
%fastboot% flash modemfirmware_ab images\modemfirmware.img
%fastboot% flash multiimgqti_ab images\multiimgqti.img
%fastboot% flash pdp_ab images\pdp.img
%fastboot% flash pdp_cdb_ab images\pdp_cdb.img
%fastboot% flash pvmfw_ab images\pvmfw.img
%fastboot% flash qtvm_dtbo_ab images\qtvm_dtbo.img
%fastboot% flash qupfw_ab images\qupfw.img
%fastboot% flash secretkeeper_ab images\secretkeeper.img
%fastboot% flash shrm_ab images\shrm.img
%fastboot% flash soccp_ab images\soccp.img
%fastboot% flash soccp_dcd_ab images\soccp_dcd.img
%fastboot% flash soccp_debug_ab images\soccp_debug.img
%fastboot% flash spuservice_ab images\spuservice.img
%fastboot% flash tme_config_ab images\tme_config.img
%fastboot% flash tme_fw_ab images\tme_fw.img
%fastboot% flash tme_seq_patch_ab images\tme_seq_patch.img
%fastboot% flash tz_ab images\tz.img
%fastboot% flash tz_ac_config_ab images\tz_ac_config.img
%fastboot% flash tz_qti_config_ab images\tz_qti_config.img
%fastboot% flash uefi_ab images\uefi.img
%fastboot% flash uefisecapp_ab images\uefisecapp.img
%fastboot% flash vbmeta_ab images\vbmeta.img
%fastboot% flash vbmeta_system_ab images\vbmeta_system.img
%fastboot% flash vm-bootsys_ab images\vm-bootsys.img
%fastboot% flash xbl_ab images\xbl.img
%fastboot% flash xbl_ac_config_ab images\xbl_ac_config.img
%fastboot% flash xbl_config_ab images\xbl_config.img
%fastboot% flash xbl_ramdump_ab images\xbl_ramdump.img
%fastboot% flash boot_ab images\boot.img
%fastboot% flash init_boot_ab images\init_boot.img
%fastboot% flash vendor_boot_ab images\vendor_boot.img
REM recovery NAO flasheado — preserva TWRP (reflash manual: fastboot flash recovery tools\twrp-popsicle.img)
%fastboot% flash super images\super.img.0
%fastboot% flash super images\super.img.1
%fastboot% flash super images\super.img.2
%fastboot% flash super images\super.img.3
%fastboot% flash super images\super.img.4
%fastboot% flash super images\super.img.5
%fastboot% flash super images\super.img.6
%fastboot% flash super images\super.img.7
%fastboot% flash super images\super.img.8
%fastboot% flash super images\super.img.9
%fastboot% flash super images\super.img.10
%fastboot% flash super images\super.img.11
%fastboot% flash super images\super.img.12
%fastboot% flash super images\super.img.13
%fastboot% reboot
