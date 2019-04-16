package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 列辅助插件
 *
 * @author ZhangJiacheng
 * @date 2019-04-15
 */
public class ExampleColumnPlugin extends BasePlugin {

    private static final Logger logger = LoggerFactory.getLogger(ExampleColumnPlugin.class);

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType("java.util.List");
        StringJoiner joiner = new StringJoiner(",");
        introspectedTable.getAllColumns()
                .stream()
                .map(IntrospectedColumn::getActualColumnName)
                .map(s -> "\"" + s + "\"")
                .forEach(joiner::add);
        Field columnFieldList = JavaElementGeneratorTools.generateStaticFinalField(
                "FIELD_NAME_LIST",
                new FullyQualifiedJavaType("String[]"),
                "new String[]{" + joiner.toString() + "}"
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
        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("for (String f : FIELD_NAME_LIST) {");
        bodyLines.add("if (f.equalsIgnoreCase(field)) {");
        bodyLines.add("return true;");
        bodyLines.add("}");
        bodyLines.add("}");
        bodyLines.add("return false;");
        columnExistsMethod.addBodyLines(bodyLines);
        FormatTools.addMethodWithBestPosition(topLevelClass, columnExistsMethod);
        return true;
    }
}
