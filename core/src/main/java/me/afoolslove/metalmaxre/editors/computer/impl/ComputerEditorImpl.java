package me.afoolslove.metalmaxre.editors.computer.impl;

import me.afoolslove.metalmaxre.MetalMaxRe;
import me.afoolslove.metalmaxre.editors.AbstractEditor;
import me.afoolslove.metalmaxre.editors.Editor;
import me.afoolslove.metalmaxre.editors.computer.Computer;
import me.afoolslove.metalmaxre.editors.computer.IComputerEditor;
import me.afoolslove.metalmaxre.editors.computer.listener.IComputerListener;
import me.afoolslove.metalmaxre.utils.DataAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 计算机编辑器
 * <p>
 * <p>
 * 计算机一共有4个属性，分别为：所在地图(map)、类型(type)、坐标(x,y)
 * <p>
 * 使用 (MAP*N)+(TYPE*N)+(X*N)+(Y*N) 的格式储存
 * <p>
 * e.g:
 * <p>
 * 物品A(map,type,x,y): 1,0,1,1<p>
 * 物品B(map,type,x,y): 2,0,2,2<p>
 * 物品N......<p>
 * <p>
 * 以上数据为：(12)(00)(12)(12)<p>
 */
public class ComputerEditorImpl extends AbstractEditor<IComputerListener> implements IComputerEditor<Computer> {
    protected final DataAddress computerDataAddress;
    protected int maxCount = 0x7B;

    private final Set<Computer> computers = new LinkedHashSet<>(getMaxCount());

    public ComputerEditorImpl(@NotNull MetalMaxRe metalMaxRe, DataAddress dataAddress) {
        super(metalMaxRe);
        this.computerDataAddress = dataAddress;
    }

    @Editor.Load
    public void onLoad() {
        // 初始化计算机
        getComputers().clear();

        // data[0] = map
        // data[1] = type
        // data[2] = x
        // data[3] = y
        byte[][] data = new byte[4][getMaxCount()];
        getBuffer().getAABytes(getComputerDataAddress().getStartAddress(), 0, getMaxCount(), data);

        for (int i = 0; i < getMaxCount(); i++) {
            getComputers().add(new Computer(data[0][i], data[1][i], data[2][i], data[3][i]));
        }

    }

    @Editor.Apply
    public void onApply() {
        var computers = new ArrayList<>(getComputers());
        int count = Math.min(getMaxCount(), computers.size());

        // data[0] = map
        // data[1] = type
        // data[2] = x
        // data[3] = y
        byte[][] data = new byte[4][getMaxCount()];

        for (int i = 0; i < count; i++) {
            var computer = computers.get(i);
            data[0][i] = computer.getMap();
            data[1][i] = computer.getType();
            data[2][i] = computer.getX();
            data[3][i] = computer.getY();
        }

        // 如果有空的计算机，将空的计算机设置到地图 0xFF 中去
        var remain = getMaxCount() - count;
        if (remain > 0) {
            Arrays.fill(data[0], count, getMaxCount(), (byte) 0xFF);
        }

        getBuffer().put(getComputerDataAddress(), data);
    }

    @Override
    public DataAddress getComputerDataAddress() {
        return computerDataAddress;
    }

    @Override
    public int getMaxCount() {
        return maxCount;
    }

    @Override
    public Set<Computer> getComputers() {
        return computers;
    }

    @Override
    public void addComputer(@NotNull Computer computer) {
        computers.add(computer);
    }

    @Override
    public void removeComputer(@NotNull Computer computer) {
        computers.remove(computer);
    }

    @Override
    public boolean replaceComputer(@Nullable Computer source, @NotNull Computer replace) {
        if (computers.contains(replace)) {
            return true;
        }
        if (source == null) {
            return false;
        }
        if (computers.remove(source)) {
            // 移除旧计算机成功，添加新的计算机
            return computers.add(replace);
        }
        return false;

    }

    /**
     * 通用地址
     */
    public static class JapaneseComputerEditor extends ComputerEditorImpl {

        public JapaneseComputerEditor(@NotNull MetalMaxRe metalMaxRe) {
            super(metalMaxRe, DataAddress.from(0x39DD2 - 0x10, 0x39FB0 - 0x10));
        }
    }
}
