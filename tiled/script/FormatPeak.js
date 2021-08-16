// author AFoolLove
// tiled version 1.7.2
//
// 可以将导出的tmx世界地图中的大山修改为和游戏中一样的高低显示，而非同一个图块
// e.g: ------ to /----\
// tile.tileset.tile(0x0A); 山脉起始
// tile.tileset.tile(0x0B); 山脉结束
var action = tiled.registerAction("FormatPeak", function(action){
    // 得到当前地图
    var world = tiled.activeAsset;
    if(world == null || !world.isTileMap){
        // 无效或未选中地图
         tiled.alert("请选中需要被格式化的地图！")
        return;
    }
    for(var i = 0; i < world.layerCount; i++){
        // 获取到 "world" 层
        var layer = world.layerAt(i);
        if (layer.isTileLayer && layer.name == "world"){
            // 获取该层的编辑器
            var editLayer = layer.edit();
            // 连续山脉的计数器
            var continuous = 0;
            for(var index = 0; index < (0x100 * 0x100); index++){
                var x = index % 0x100;
                var y = index / 0x100;
                var offset = (y & 1) == 1 ? 0 : 1;

                // 在新行中，如果上一行的山脉未结束，立即结束并重置连续山脉计数器
                if(x == 0x00){
                    if(continuous > 1) {
                        // 上一行未结束，立即结束山脉
                        var lastPeak = tile.tileset.tile(0x0B);
                        editLayer.setTile((index - 1) % 0x100, (index - 1) / 0x100, lastPeak);
                    }
                    // 重置连续山脉计数器
                    continuous = 0x00;
                }

                var tile = layer.tileAt(x, y);
                if(tile == null){
                    continue;
                }
                // 获取当前图块是否为山脉
                // 如果山脉刚开始计数，设置山脉起始
                // 否则判断山脉是否结束，设置山脉结束
                if(tile.id == 0x02){
                    if(continuous == 0x00){
                        // 设置山脉起始
                        var firstPeak = tile.tileset.tile(0x0A);
                        editLayer.setTile(x, y, firstPeak);
                    } else {
                        editLayer.setTile(x, y, tile.tileset.tile(0x02 + ((x + offset) % 2)));
                    }
                    continuous++;
                } else {
                    if(continuous <= 1){
                        continue;
                    }
                    // 设置山脉结束
                    var lastPeak = tile.tileset.tile(0x0B);
                    editLayer.setTile((index - 1) % 0x100, (index - 1) / 0x100, lastPeak);
                    continuous = 0;
                }
            }
            editLayer.apply();
            print("Format Peak OK.")
            break;
        }
    }
})
action.text = "Format Peak"

tiled.extendMenu("Map", [
    {action:"FormatPeak"},
    {separator: true}
])