package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringJoiner;

/**
 * 列辅助插件
 *
 * @author LanyuanXiaoyao
 * @date 2019-04-15
 */
public class ExampleColumnHelperPlugin extends BasePlugin {

    private static final Logger logger = LoggerFactory.getLogger(ExampleColumnHelperPlugin.class);

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType("java.util.List");
        topLevelClass.addImportedType("java.util.Arrays");
        StringJoiner joiner = new StringJoiner(",");
        introspectedTable.getAllColumns()
                .stream()
                .map(introspectedColumn -> "{\"" + introspectedColumn.getActualColumnName() + "\", \"" + introspectedColumn.getJavaProperty() + "\"}")
                .forEach(joiner::add);
        Field columnFieldList = JavaElementGeneratorTools.generateStaticFinalField(
                "FIELD_NAME_LIST",
                new FullyQualifiedJavaType("String[][]"),
                "new String[][]{" + joiner.toString() + "}"
        );
        columnFieldList.setStatic(true);
        commentGenerator.addFieldComment(columnFieldList, introspectedTable);
        topLevelClass.addField(columnFieldList);

        Method columnExistsMethod = JavaElementGeneratorTools.generateMethod(
                "existsColumn",
                JavaVisibility.PRIVATE,
                FullyQualifiedJavaType.getBooleanPrimitiveInstance(),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "field")
        );
        columnExistsMethod.setStatic(true);
        commentGenerator.addGeneralMethodComment(columnExistsMethod, introspectedTable);
        columnExistsMethod.addBodyLine("return Arrays.stream(FIELD_NAME_LIST).anyMatch(strings -> strings[0].equalsIgnoreCase(field) || strings[0].equalsIgnoreCase(field));");
        FormatTools.addMethodWithBestPosition(topLevelClass, columnExistsMethod);

        Method getActualColumnName = JavaElementGeneratorTools.generateMethod(
                "getActualColumnName",
                JavaVisibility.PRIVATE,
                FullyQualifiedJavaType.getStringInstance(),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "field")
        );
        getActualColumnName.setStatic(true);
        commentGenerator.addGeneralMethodComment(getActualColumnName, introspectedTable);
        getActualColumnName.addBodyLine("return Arrays.stream(FIELD_NAME_LIST).filter(strings -> strings[0].equalsIgnoreCase(field) || strings[1].equalsIgnoreCase(field)).findFirst().map(strings -> strings[0]).orElse(\"\");");
        FormatTools.addMethodWithBestPosition(topLevelClass, getActualColumnName);

        return true;
    }
}
