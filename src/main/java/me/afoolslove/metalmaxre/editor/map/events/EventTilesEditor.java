package me.afoolslove.metalmaxre.editor.map.events;

import me.afoolslove.metalmaxre.editor.AbstractEditor;
import me.afoolslove.metalmaxre.editor.EditorManager;
import me.afoolslove.metalmaxre.editor.map.MapEditor;
import me.afoolslove.metalmaxre.editor.map.MapProperties;
import me.afoolslove.metalmaxre.editor.map.MapPropertiesEditor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 事件图块编辑器
 * 支持世界地图
 * 根据事件状态可以显示不同的图块，覆盖原图块
 * 注意尽量不要同时作用在同一个图块上，因为没测试会发生啥
 * <p>
 * 需要地图属性中的事件图块启用才会生效
 * 图块图像根据玩家当前的图块组合不同而不同
 * <p>
 * <p>
 * 世界地图：
 * 世界地图的总tile大小为0x100*0x100，其中每4*4为一个小块
 * 当世界地图使用时，单个tile数据控制此4*4的方块
 * 并且X、Y的计算方式变更为 X*4、Y*4，X、Y < 0x40
 * <p>
 * <p>
 * 2021年5月26日：已完成并通过测试基本编辑功能
 *
 * @author AFoolLove
 */
public class EventTilesEditor extends AbstractEditor {

    /**
     * K：Map
     * V：events
     */
    private final HashMap<Integer, Map<Integer, List<EventTile>>> eventTiles = new HashMap<>();

    @Override

    public boolean onRead(@NotNull ByteBuffer buffer) {
        // 读取前清空数据
        eventTiles.clear();

        // 排除事件为 0x00 ！！！！
        // buffer.position(0x1DCCF);

        // 填充
        for (int i = 0; i < MapEditor.MAP_MAX_COUNT; i++) {
            getEventTiles().put(i, new HashMap<>());
        }

        MapPropertiesEditor mapPropertiesEditor = EditorManager.getEditor(MapPropertiesEditor.class);

        var map = new HashMap<>(mapPropertiesEditor.getMapProperties())
                .entrySet().stream().parallel()
                .filter(entry -> entry.getValue().hasEventTile()) // 移除没有事件图块属性的地图
                .collect(
                        // 移除相同的事件图块数据索引
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing((o) -> o.getValue().eventTilesIndex)))
                );

        for (Map.Entry<Integer, MapProperties> mapPropertiesEntry : map) {
            char eventTilesIndex = mapPropertiesEntry.getValue().eventTilesIndex;
            buffer.position(0x10 + 0x1C000 + eventTilesIndex - 0x8000);

            // 一个或多个事件作为一组，一组使用 0x00 作为结尾
            var events = new HashMap<Integer, List<EventTile>>();
            // 事件
            int event = buffer.get();
            do {
                // 图块数量
                int count = buffer.get();

                List<EventTile> eventTiles = new ArrayList<>();
                // 读取事件图块：X、Y、图块
                for (int i = count; i > 0; i--) {
                    eventTiles.add(new EventTile(buffer.get(), buffer.get(), buffer.get()));
                }
                events.put(event, eventTiles);
            } while ((event = buffer.get()) != 0x00);

            // 添加事件图块组
            for (Map.Entry<Integer, MapProperties> propertiesEntry : mapPropertiesEditor.getMapProperties().entrySet()) {
                if (propertiesEntry.getValue().eventTilesIndex == eventTilesIndex) {
                    // 添加使用当前事件图块的地图
                    getEventTiles().put(propertiesEntry.getKey(), events);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onWrite(@NotNull ByteBuffer buffer) {
        MapPropertiesEditor mapPropertiesEditor = EditorManager.getEditor(MapPropertiesEditor.class);

        // 排除事件为 0x00 ！！！！
        getEventTiles().values().forEach(each -> {
            each.entrySet().removeIf(entry -> entry.getKey() == 0x00);
        });

        buffer.position(0x1DCCF);

        getEventTiles().values()
                .stream().parallel()
                .filter(entry -> !entry.isEmpty()) // 过滤没有事件图块的地图
                .distinct()
                .forEachOrdered(events -> {
                    // 计算新的事件图块索引，太长了！简称：索引
                    char newEventTilesIndex = (char) (buffer.position() - 0x10 - 0x1C000 + 0x8000);
                    // 将旧的索引替换为新的索引
                    getEventTiles().entrySet()
                            .stream().parallel()
                            .filter(entry1 -> entry1.getValue() == events) // 获取相同事件图块的地图
                            .forEach(mapEntry -> {
                                // 通过相同的事件图块组更新索引
                                mapPropertiesEditor.getMapProperties(mapEntry.getKey()).eventTilesIndex = newEventTilesIndex;
                            });

                    // 写入数据
                    for (Map.Entry<Integer, List<EventTile>> eventsList : events.entrySet()) {
                        // 写入事件
                        buffer.put(eventsList.getKey().byteValue());
                        // 写入事件数量
                        buffer.put(((byte) eventsList.getValue().size()));
                        // 写入 X、Y、Tile
                        for (EventTile eventTile : eventsList.getValue()) {
                            buffer.put(eventTile.toArray());
                        }
                    }
                    // 写入事件组结束符
                    buffer.put((byte) 0x00);
                });

        int end = buffer.position() - 1;
        if (end <= 0x1DEAF) {
            System.out.printf("事件图块编辑器：剩余%d个空闲字节\n", 0x1DEAF - end);
        } else {
            System.out.printf("事件图块编辑器：错误！超出了数据上限%d字节\n", end - 0x1DEAF);
        }
        return true;
    }

    public HashMap<Integer, Map<Integer, List<EventTile>>> getEventTiles() {
        return eventTiles;
    }

    /**
     * @return 获取指定map的事件图块，可能为null，包含世界地图
     */
    public Map<Integer, List<EventTile>> getEventTile(int map) {
        return eventTiles.get(map);
    }
}
