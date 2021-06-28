package me.afoolslove.metalmaxre.editor.monster;

import me.afoolslove.metalmaxre.editor.AbstractEditor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * 怪物编辑器
 * <p>
 * 怪物种类只有 0x83(131)
 *
 * @author AFoolLove
 */
public class MonsterEditor extends AbstractEditor {
    /**
     * 怪物种类的总数量
     */
    public static final int MONSTER_COUNT = 0x83;
    /**
     * 怪物出手攻击的速度
     */
    public static final int MONSTER_SPEEDS_OFFSET = 0x38BA0 - 0x10;

    /**
     * 怪物的掉落物
     * 注：ID范围为0x18-0x82才能设置有效的战利品
     */
    public static final int MONSTER_DROPS_ITEMS_OFFSET = 0x2253F - 0x10;

    private final HashMap<Integer, Monster> monsters = new HashMap<>();

    @Override
    public boolean onRead(@NotNull ByteBuffer buffer) {
        // 读取前清空数据
        monsters.clear();

        byte[] speeds = new byte[MONSTER_COUNT];
        byte[] dropsItems = new byte[MONSTER_COUNT - 0x18];

        // 读取怪物出手攻击速度
        setPrgRomPosition(buffer, MONSTER_SPEEDS_OFFSET);
        buffer.get(speeds);

        // 读取怪物掉落物
        setPrgRomPosition(buffer, MONSTER_DROPS_ITEMS_OFFSET);
        buffer.get(dropsItems);


        for (int monsterId = 0; monsterId < MONSTER_COUNT; monsterId++) {
            Monster monster = new Monster();
            if (monsterId >= 0x18) {
                monster.setDropsItem(dropsItems[monsterId]);
            }
            monster.setSpeed(speeds[monsterId]);
            monsters.put(monsterId, monster);
        }
        return true;
    }

    @Override
    public boolean onWrite(@NotNull ByteBuffer buffer) {

        return true;
    }
}
