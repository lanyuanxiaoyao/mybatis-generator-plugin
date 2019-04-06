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

public class SelectLikeByModelPlugin extends BasePlugin {

    private static final String SELECT_LIKE_MODEL_METHOD_NAME = "selectLikeByModel";

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Method selectLikeModelMethod = JavaElementGeneratorTools.generateMethod(
                SELECT_LIKE_MODEL_METHOD_NAME,
                JavaVisibility.DEFAULT,
                new FullyQualifiedJavaType("List<" + introspectedTable.getBaseRecordType() + ">"),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "record")
        );
        commentGenerator.addGeneralMethodComment(selectLikeModelMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, selectLikeModelMethod);

        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        XmlElement allColumn = new XmlElement("include");
        allColumn.addAttribute(new Attribute("refid", "Base_Column_List"));
        List<IntrospectedColumn> columnList = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());

        XmlElement selectLikeModel = new XmlElement("select");
        selectLikeModel.addAttribute(new Attribute("id", SELECT_LIKE_MODEL_METHOD_NAME));
        selectLikeModel.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        selectLikeModel.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        commentGenerator.addComment(selectLikeModel);

        selectLikeModel.addElement(new TextElement("select"));
        selectLikeModel.addElement(allColumn);
        selectLikeModel.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        selectLikeModel.addElement(new TextElement("where 1=1"));

        XmlElement checkObject2 = new XmlElement("if");
        checkObject2.addAttribute(new Attribute("test", "_parameter != null"));
        columnList.forEach(column -> {
            XmlElement check = new XmlElement("if");
            check.addAttribute(new Attribute("test", String.format(
                    "%s != null",
                    column.getJavaProperty()
            )));
            String javaProperty = column.getJavaProperty();
            check.addElement(new TextElement(String.format(
                    "and %s like #{%s, jdbcType=%s}",
                    column.getActualColumnName(),
                    javaProperty,
                    column.getJdbcTypeName()
            )));
            checkObject2.addElement(check);
        });
        selectLikeModel.addElement(checkObject2);

        document.getRootElement().addElement(selectLikeModel);

        return true;
    }
}
