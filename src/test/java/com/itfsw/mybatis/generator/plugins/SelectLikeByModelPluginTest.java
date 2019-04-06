package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.tools.AbstractShellCallback;
import com.itfsw.mybatis.generator.plugins.tools.MyBatisGeneratorTool;
import com.itfsw.mybatis.generator.plugins.tools.Util;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;

public class SelectLikeByModelPluginTest {

    @BeforeClass
    public static void init() {

    }

    @Test
    public void testCreateMethod() throws Exception {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/selectByModelPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception {
                Class clsTbMapper = loader.loadClass(packagz + ".TbMapper");
                for (Method method : clsTbMapper.getDeclaredMethods()) {
                    if (method.getName().equals("selectByModel")) {
                        Assert.assertEquals(Util.getListActualType(method.getGenericParameterTypes()[0]), packagz + ".Tb");
                    }
                }
            }
        });
    }

}
