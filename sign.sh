
gradle assembleRelease

MY_SDK_PATH=~/Library/Android/sdk/build-tools/25.0.2
echo "Signer batch uses SDK of $MY_SDK_PATH"

BUILD_OUTPUT=./app/build/outputs/apk

echo "Using zipalign"
rm $BUILD_OUTPUT/app-release-unsigned-aligned.apk 
$MY_SDK_PATH/zipalign -v -p 4 $BUILD_OUTPUT/app-release-unsigned.apk $BUILD_OUTPUT/app-release-unsigned-aligned.apk

echo "Signing using local folder key"
$MY_SDK_PATH/apksigner sign --ks my-release-key.keystore --out app-release-signed.apk $BUILD_OUTPUT/app-release-unsigned-aligned.apk

echo "Verifiying the signing"
$MY_SDK_PATH/apksigner verify app-release-signed.apk

echo "Align and Sign are complete"


