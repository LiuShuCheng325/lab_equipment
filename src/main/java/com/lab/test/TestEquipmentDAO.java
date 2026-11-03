package com.lab.test;

import com.lab.dao.EquipmentDAO;
import com.lab.dao.impl.EquipmentDAOImpl;
import com.lab.entity.Equipment;
import java.util.List;

public class TestEquipmentDAO {
    public static void main(String[] args) {
        // 1. 创建DAO对象
        EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
        // 2. 调用查询全部方法
        List<Equipment> equipmentList = equipmentDAO.findAll();

        System.out.println("======== 查询到设备总数：" + equipmentList.size() + " ========");
        // 3. 遍历打印每条设备
        for (Equipment eq : equipmentList) {
            System.out.println("ID:" + eq.getId()
                    + " 名称:" + eq.getEqName()
                    + " 型号:" + eq.getModel()
                    + " 图片:" + eq.getImg());
        }
    }
}