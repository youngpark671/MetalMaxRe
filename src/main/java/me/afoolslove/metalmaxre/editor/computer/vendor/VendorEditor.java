package me.afoolslove.metalmaxre.editor.computer.vendor;

import me.afoolslove.metalmaxre.editor.AbstractEditor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 售货机商品编辑器
 * <p>
 * 起始：0x23EC8
 * 结束：0x23FC4
 * <p>
 * 0x00 固定值 0x0D
 * 0x01 - 0x06  商品类型
 * 0x07 - 0x0C  商品数量
 * 0x0D 中奖物品
 * 数量byte D7的值为1时，商品为无货
 * <p>
 * TODO 商品价格，毫无头绪，有了，但是没有测试（刚看见还有这个todo
 *
 * @author AFoolLove
 */
public class VendorEditor extends AbstractEditor {
    /**
     * 售货机商品的最大组合数量
     */
    public static final int VENDOR_MAX_COUNT = 0x12;

    /**
     * 售货机数据地址起始
     */
    public static final int VENDOR_START_OFFSET = 0x23EC8 - 0x10;
    /**
     * 售货机数据地址结束
     */
    public static final int VENDOR_END_OFFSET = 0x23FC4 - 0x10;

    /**
     * 所有售货机的商品组合
     */
    private final List<VendorItemList> vendorItemLists = new ArrayList<>();

    @Override
    public boolean onRead(@NotNull ByteBuffer buffer) {
        // 读取前清空数据
        vendorItemLists.clear();

        byte[] items = new byte[0x06];
        byte[] counts = new byte[0x06];
        // 读取售货机的商品组合
        setPrgRomPosition(buffer, VENDOR_START_OFFSET);
        while (buffer.get() == 0x0D) {
            // 读取商品
            buffer.get(items);
            // 读取商品数量和是否有货
            buffer.get(counts);

            VendorItemList itemList = new VendorItemList();
            // 添加商品
            for (int i = 0; i < 0x06; i++) {
                itemList.add(new VendorItem(items[i], counts[i]));
            }
            // 获取中奖物品
            itemList.setAward(buffer.get());
            vendorItemLists.add(itemList);
        }
        return true;
    }

    @Override
    public boolean onWrite(@NotNull ByteBuffer buffer) {
        // 移除多余的商品组合
        Iterator<VendorItemList> iterator = vendorItemLists.iterator();
        limit(iterator, () -> vendorItemLists.size() > VENDOR_MAX_COUNT, remove -> {
            System.out.printf("售货机编辑器：移除多余的售货机商品组 %s", remove);
        });

        byte[] items = new byte[0x06];
        byte[] counts = new byte[0x06];
        setPrgRomPosition(buffer, VENDOR_START_OFFSET);
        while (iterator.hasNext()) {
            VendorItemList vendorItemList = iterator.next();

            // 写入固定头
            buffer.put((byte) 0x0D);
            // 写入商品和数量
            for (int i = 0; i < 0x06; i++) {
                VendorItem item = vendorItemList.get(i);
                items[i] = item.item;
                counts[i] = item.count;
            }
            buffer.put(items);
            buffer.put(counts);
            // 写入奖品
            buffer.put(vendorItemList.award);
        }
        return true;
    }

    /**
     * @return 所有售货机的商品
     */
    public List<VendorItemList> getVendorItemLists() {
        return vendorItemLists;
    }

    /**
     * @return 某一个售货机的商品
     */
    public VendorItemList getVendorItemList(int vendor) {
        return vendorItemLists.get(vendor);
    }
}
