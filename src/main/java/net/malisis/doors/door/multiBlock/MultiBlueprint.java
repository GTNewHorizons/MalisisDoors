package net.malisis.doors.door.multiBlock;

import java.util.Map;

import org.joml.Vector3i;

public class MultiBlueprint {

    public enum RotationDegrees {
        ROT90,
        ROT180,
        ROT270,
    }

    public final static int MB = Integer.MAX_VALUE; // Main Block
    public final static int EMPTY = -1;
    public final static int RM = Integer.MIN_VALUE;

    public int[][][] bluePrint;
    public Vector3i startingLocation;
    public int yLength;
    public int xLength;
    public int zLength;
    private Map<Integer, int[]> metaMap;

    public MultiBlueprint(int[][][] print, Map<Integer, int[]> metaMap, Vector3i startingLocation) {
        this.yLength = print.length;
        this.xLength = print[0].length;
        this.zLength = print[0][0].length;
        this.bluePrint = print;
        this.metaMap = metaMap;
        this.startingLocation = startingLocation;
    }

    public void rotate(RotationDegrees angle) {
        int[][][] rotatedPrint = new int[bluePrint.length][bluePrint.length][bluePrint.length];
        switch (angle) {
            case ROT90 -> {
                for (int i = 0; i < bluePrint.length; i++) {
                    rotatedPrint[i] = rotate90Clockwise(bluePrint[i]);
                }
            }
            case ROT180 -> {
                for (int i = 0; i < bluePrint.length; i++) {
                    rotatedPrint[i] = rotate180Clockwise(bluePrint[i]);
                }
            }
            case ROT270 -> {
                for (int i = 0; i < bluePrint.length; i++) {
                    rotatedPrint[i] = rotate270Clockwise(bluePrint[i]);

                }
            }
        }
        convertMeta(rotatedPrint, angle);
        this.bluePrint = rotatedPrint;
    }

    private int[][] rotate90Clockwise(int[][] matrix) {
        return transpose(reverseColumns(matrix));
    }

    private int[][] rotate180Clockwise(int[][] matrix) {
        return reverseColumns(reverseRows(matrix));
    }

    private int[][] rotate270Clockwise(int[][] matrix) {
        return transpose(reverseRows(matrix));
    }

    private void convertMeta(int[][][] mat, RotationDegrees angle) {
        int conversionInfo = angle.ordinal();
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                for (int k = 0; k < mat[i][j].length; k++) {
                    int[] newState = metaMap.get(mat[i][j][k]);
                    if (newState == null) {
                        if (mat[i][j][k] == MB) {
                            this.startingLocation = new Vector3i(j, i, k);
                        }
                    } else {
                        mat[i][j][k] = newState[conversionInfo];
                    }
                }
            }
        }
    }

    private int[][] transpose(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] transposed = new int[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    private int[][] reverseColumns(int[][] matrix) {
        return multiplyMatrices(generateRowReversalMatrix(matrix.length), matrix);
    }

    private int[][] reverseRows(int[][] matrix) {
        return multiplyMatrices(matrix, generateRowReversalMatrix(matrix[0].length));
    }

    private int[][] generateRowReversalMatrix(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            matrix[i][size - 1 - i] = 1;
        }
        return matrix;
    }

    private int[][] multiplyMatrices(int[][] A, int[][] B) {
        int rowsA = A.length;
        int rowsB = B.length;
        int colsA = A[0].length;
        int colsB = B[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException(
                "Number of columns in Matrix A must be equal to number of rows in Matrix B.");
        }

        int[][] result = new int[rowsA][colsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                result[i][j] = 0;
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return result;
    }
}
