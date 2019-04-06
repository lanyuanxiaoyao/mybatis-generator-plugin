package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;

import java.util.List;

public class SelectBetweenByModelPlugin extends BasePlugin {

    private static final String METHOD_NAME = "selectBetweenModel";

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        Method selectBetweenByModelMethod = JavaElementGeneratorTools.generateMethod(
                METHOD_NAME,
                JavaVisibility.DEFAULT,
                new FullyQualifiedJavaType("List<" + introspectedTable.getBaseRecordType() + ">"),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "start", "@Param(\"start\")"),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "end", "@Param(\"end\")")
        );
        commentGenerator.addGeneralMethodComment(selectBetweenByModelMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, selectBetweenByModelMethod);

        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        XmlElement selectElement = new XmlElement("select");
        selectElement.addAttribute(new Attribute("id", METHOD_NAME));
        selectElement.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        selectElement.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        commentGenerator.addComment(selectElement);

        selectElement.addElement(new TextElement("select"));
        XmlElement allColumn = new XmlElement("include");
        allColumn.addAttribute(new Attribute("refid", "Base_Column_List"));
        selectElement.addElement(allColumn);
        selectElement.addElement(new TextElement("form " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        selectElement.addElement(new TextElement("where 1=1"));

        XmlElement checkObject = new XmlElement("if");
        checkObject.addAttribute(new Attribute("test", "start != null and end != null"));
        List<IntrospectedColumn> columnList = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        columnList.forEach(column -> {
            String javaPropertyName = column.getJavaProperty();
            String jdbcTypeName = column.getJdbcTypeName();
            XmlElement check = new XmlElement("if");
            check.addAttribute(new Attribute("test", String.format(
                    "start.%s != null and end.%s != null",
                    javaPropertyName,
                    javaPropertyName
            )));
            check.addElement(new TextElement(String.format(
                    "and %s between #{start.%s, jdbcType=%s } and #{end.%s, jdbcType=%s }",
                    column.getActualColumnName(),
                    javaPropertyName,
                    jdbcTypeName,
                    javaPropertyName,
                    jdbcTypeName
            )));
            checkObject.addElement(check);
        });
        selectElement.addElement(checkObject);

        document.getRootElement().addElement(selectElement);

        return true;
    }
}
