package brunocamarggo.lox;

public class Interpreter implements Expr.Visitor<Object> {

    void interpret(Expr expression) {
        try {
            var value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            var text = object.toString();
            if(text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        var left = evaluate(expr.left);
        var right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            }
            case BANG_EQUAL -> { return !isEqual(left, right); }
            case EQUAL_EQUAL -> { return isEqual(left, right); }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                if( (double) right == 0) {
                    throw new RuntimeError(expr.operator, "Error division by 0.");
                }
                return (double) left / (double) right;
            }
            case STAR ->  {
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            }
            case PLUS -> {
                if (left instanceof Double leftAsDouble && right instanceof Double rightAsDouble) {
                    return leftAsDouble + rightAsDouble;
                }

                if (left instanceof String leftAsString && right instanceof String rightAsString) {
                    return leftAsString + rightAsString;
                }

                if(left instanceof String leftAsString && right instanceof Double rightAsDouble) {
                    return leftAsString + stringify(rightAsDouble);
                }

                if(left instanceof Double leftAsDouble && right instanceof String rightAsString) {
                    return stringify(leftAsDouble) + rightAsString;
                }

                throw new RuntimeError(expr.operator, "Operans must be two numbers or two strings.");
            }
        }

        return null;
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        var right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                return - (double) right; }
            case BANG -> { return !isTruthy(right); }
        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw  new RuntimeError(operator, "Operand must be a number.");
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }
}
