package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.HashMap;
import java.util.Map;

public class AutoTableFieldCommentPlugin extends BasePlugin {

	/*
	 * Field添加注释(注释为表中的remarks字段)
	 * 
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		try {
			Map<String, IntrospectedColumn> columnMap = new HashMap<>();
			introspectedTable.getAllColumns().forEach(column -> columnMap.put(column.getJavaProperty(), column));
			topLevelClass.getFields().stream().peek(field -> {
				try {
					IntrospectedColumn column = columnMap.get(field.getName());

					StringBuilder sb = new StringBuilder();
					sb.append(" * ");
					sb.append(column.getRemarks());
					
					field.addJavaDocLine("/**");
					field.addJavaDocLine(sb.toString().replace("\n", " "));
					field.addJavaDocLine(" */");

				} catch (Exception ignored) {
				}
			});
		} catch (Exception e) {
			topLevelClass.addAnnotation(e.getMessage());
		}
		return true;
	}
}
