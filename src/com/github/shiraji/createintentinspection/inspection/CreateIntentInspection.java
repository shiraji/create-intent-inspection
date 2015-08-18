package com.github.shiraji.createintentinspection.inspection;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class CreateIntentInspection extends BaseJavaLocalInspectionTool {
    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "Android";
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Activity should implement createIntent()";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new CreateIntentInspectionVisitor(holder, isOnTheFly);
    }
}
