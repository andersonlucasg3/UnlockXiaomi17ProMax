#!/system/bin/sh
# Repro do bug do apply_ssaid.sh em sandbox (/data/local/tmp/ssaidlab)
# Recria o estado exato de 19:03: XML original -> randomize manual 15:38 -> apply WebUI
LAB=/data/local/tmp/ssaidlab
MODDIR_ORIG=/data/adb/modules/deviceidchanger
rm -rf $LAB; mkdir -p $LAB

# 1. estado 15:35 (original restaurado)
cp /data/system/users/0/settings_ssaid.xml $LAB/input.abx || exit 1
abx2xml $LAB/input.abx $LAB/pre.xml || exit 1

# 2. recria o randomize manual das 15:38 (ID compartilhado nos 3 Caixa)
sed -i 's|\(name="10292" value="\)[0-9a-f]*|\108c406642b2f299d|; s|\(name="10346" value="\)[0-9a-f]*|\108c406642b2f299d|; s|\(name="10361" value="\)[0-9a-f]*|\108c406642b2f299d|' $LAB/pre.xml
xml2abx $LAB/pre.xml $LAB/input2.abx || exit 1
ls -l $LAB/input.abx $LAB/input2.abx

# 3. .apply_list do usuario (superapp custom)
printf 'br.gov.caixa.superapp 10346 b1d17a22eacb3def\n' > $LAB/.apply_list

# 4. apply_ssaid.sh com paths pro lab
sed -e "s|MODDIR=/data/adb/modules/deviceidchanger|MODDIR=$LAB|" \
    -e "s|SSAID=/data/system/users/0/settings_ssaid.xml|SSAID=$LAB/input2.abx|" \
    $MODDIR_ORIG/apply_ssaid.sh > $LAB/apply.sh
sh $LAB/apply.sh; echo apply_exit=$?

# 5. resultado: tamanho + decode + diff
ls -l $LAB/input2.abx
abx2xml $LAB/input2.abx $LAB/post.xml || echo DECODE_FALHOU
if [ -f $LAB/post.xml ]; then
  echo "--- diff pre vs post ---"
  diff $LAB/pre.xml $LAB/post.xml | head -20
  echo "--- contagem de settings ---"
  grep -c '<setting ' $LAB/pre.xml
  grep -c '<setting ' $LAB/post.xml
fi
