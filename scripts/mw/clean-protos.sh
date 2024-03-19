# For Android app internal usage, we don't need the following protos:

echo "Cleaning protos... "

rm -rf protocol/src/main/proto/service.proto
rm -rf protocol/src/main/proto/block.proto
rm -rf protocol/src/main/proto/file.proto
rm -rf protocol/src/main/proto/snapshot.proto
rm -rf protocol/src/main/proto/migration.proto

echo "Done with cleaning protos!"