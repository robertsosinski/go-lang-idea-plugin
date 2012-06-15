package ro.redeul.google.go.lang.documentation;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import ro.redeul.google.go.lang.parser.GoElementTypes;
import ro.redeul.google.go.lang.psi.GoFile;
import ro.redeul.google.go.lang.psi.declarations.GoConstDeclaration;
import ro.redeul.google.go.lang.psi.declarations.GoConstDeclarations;
import ro.redeul.google.go.lang.psi.declarations.GoVarDeclaration;
import ro.redeul.google.go.lang.psi.declarations.GoVarDeclarations;
import ro.redeul.google.go.lang.psi.expressions.literals.GoLiteralIdentifier;
import ro.redeul.google.go.lang.psi.toplevel.GoFunctionDeclaration;
import ro.redeul.google.go.lang.psi.toplevel.GoMethodDeclaration;
import ro.redeul.google.go.lang.psi.toplevel.GoTypeDeclaration;
import ro.redeul.google.go.lang.psi.toplevel.GoTypeNameDeclaration;
import ro.redeul.google.go.lang.psi.toplevel.GoTypeSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ro.redeul.google.go.lang.psi.utils.GoPsiUtils.isNodeOfType;

class DocumentUtil {

    public static String getTailingDocumentOfElement(PsiElement element) {
        boolean foundNewLine = false;
        List<String> comments = new ArrayList<String>();
        while ((element = element.getNextSibling()) != null) {
            if (isNodeOfType(element, GoElementTypes.COMMENTS)) {
                foundNewLine = false;
                comments.add(getCommentText(element));
            } else if (isNodeOfType(element, GoElementTypes.wsNLS)) {
                if (foundNewLine || StringUtil.countChars(element.getText(), '\n') > 1) {
                    break;
                }

                foundNewLine = true;
            } else if (!(element instanceof PsiWhiteSpace)) {
                break;
            }
        }

        return StringUtil.join(comments, "\n");
    }

    public static String getHeaderDocumentOfElement(PsiElement element) {
        boolean foundNewLine = false;
        List<String> comments = new ArrayList<String>();
        while ((element = element.getPrevSibling()) != null) {
            if (isNodeOfType(element, GoElementTypes.COMMENTS)) {
                foundNewLine = false;
                comments.add(getCommentText(element));
            } else if (isNodeOfType(element, GoElementTypes.wsNLS)) {
                if (foundNewLine || StringUtil.countChars(element.getText(), '\n') > 1) {
                    break;
                }
                foundNewLine = true;
            } else if (!(element instanceof PsiWhiteSpace)) {
                break;
            }
        }

        Collections.reverse(comments);
        return StringUtil.join(comments, "\n");
    }

    public static String getCommentText(PsiElement comment) {
        String text = comment.getText().trim();
        if (text.startsWith("//")) {
            return text.substring(2);
        }
        return text.substring(2, text.length() - 2);
    }

    public static String getTypeDocument(GoTypeNameDeclaration type) {
        PsiFile file = type.getContainingFile();
        if (!(file instanceof GoFile)) {
            return "";
        }

        for (GoTypeDeclaration td : ((GoFile) file).getTypeDeclarations()) {
            for (GoTypeSpec spec : td.getTypeSpecs()) {
                if (spec != null && type.isEquivalentTo(spec.getTypeNameDeclaration())) {
                    String text = getHeaderDocumentOfElement(spec);
                    return text.isEmpty() ? getHeaderDocumentOfElement(td) : text;
                }
            }
        }

        return "";
    }

    public static String getFunctionDocument(GoFunctionDeclaration function) {
        PsiFile file = function.getContainingFile();
        String name = function.getFunctionName();
        if (!(file instanceof GoFile) || name == null || name.isEmpty()) {
            return "";
        }

        for (GoFunctionDeclaration fd : ((GoFile) file).getFunctions()) {
            if (fd != null && name.equals(fd.getFunctionName())) {
                return getHeaderDocumentOfElement(fd);
            }
        }
        return "";
    }

    public static String getMethodDocument(GoMethodDeclaration method) {
        PsiFile file = method.getContainingFile();
        String name = method.getFunctionName();
        if (!(file instanceof GoFile) || name == null || name.isEmpty()) {
            return "";
        }

        for (GoFunctionDeclaration fd : ((GoFile) file).getMethods()) {
            if (fd != null && name.equals(fd.getFunctionName())) {
                return getHeaderDocumentOfElement(fd);
            }
        }
        return "";
    }

    public static String getVarDocument(GoLiteralIdentifier id) {
        PsiFile file = id.getContainingFile();
        String name = id.getName();
        if (!(file instanceof GoFile) || name == null || name.isEmpty()) {
            return "";
        }

        for (GoVarDeclarations vds : ((GoFile) file).getGlobalVariables()) {
            GoVarDeclaration[] vdArray = vds.getDeclarations();
            for (GoVarDeclaration vd : vdArray) {
                for (GoLiteralIdentifier newId : vd.getIdentifiers()) {
                    if (name.equals(newId.getName())) {
                        String doc = getTailingDocumentOfElement(vd);
                        if (doc.isEmpty()) {
                            doc = getHeaderDocumentOfElement(vd);
                        }
                        if (doc.isEmpty()) {
                            if (vdArray.length == 1) {
                                doc = getTailingDocumentOfElement(vds);
                            }
                            if (doc.isEmpty()) {
                                doc = getHeaderDocumentOfElement(vds);
                            }
                        }
                        return doc;
                    }
                }
            }
        }

        return "";
    }

    public static String getConstDocument(GoLiteralIdentifier id) {
        PsiFile file = id.getContainingFile();
        String name = id.getName();
        if (!(file instanceof GoFile) || name == null || name.isEmpty()) {
            return "";
        }

        for (GoConstDeclarations cds : ((GoFile) file).getConsts()) {
            GoConstDeclaration[] cdArray = cds.getDeclarations();
            for (GoConstDeclaration cd : cdArray) {
                for (GoLiteralIdentifier newId : cd.getIdentifiers()) {
                    if (name.equals(newId.getName())) {
                        String doc = getTailingDocumentOfElement(cd);
                        if (doc.isEmpty()) {
                            doc = getHeaderDocumentOfElement(cd);
                        }
                        if (doc.isEmpty()) {
                            if (cdArray.length == 1) {
                                doc = getTailingDocumentOfElement(cds);
                            }
                            if (doc.isEmpty()) {
                                doc = getHeaderDocumentOfElement(cds);
                            }
                        }
                        return doc;
                    }
                }
            }
        }
        return "";
    }
}
