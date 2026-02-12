package ca.mcgill.ecse420.a1;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultiplication {

	private static final int NUMBER_THREADS = 1;
	private static final int MATRIX_SIZE = 2000;

	public static void main(String[] args) {

		double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
		double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
		sequentialMultiplyMatrix(a, b);
		parallelMultiplyMatrix(a, b);

		double[][] testA = {
		{1, 2},
		{3, 4}
		};
		double[][] testB = {
		{5, 6},
		{7, 8}
		};

		System.out.println("###### Sequential Multiplication ######");
		double[][] seqResult = sequentialMultiplyMatrix(testA, testB);
		System.out.println("###### Parallel Multiplication ######");
		double[][] parResult = parallelMultiplyMatrix(testA, testB);

		boolean matches = true;
		for (int r = 0; r < seqResult.length; r++) {
		for (int c = 0; c < seqResult[0].length; c++) {
		if (seqResult[r][c] != parResult[r][c]) {
		matches = false;
		}
		}
		}
		System.out.println("Sequential and parallel results match: " + matches);

		System.out.println("Sequential result:");
		for (int r = 0; r < seqResult.length; r++) {
		for (int c = 0; c < seqResult[0].length; c++) {
		System.out.print(seqResult[r][c]);
		if (c < seqResult[0].length - 1) {
		System.out.print(" ");
		}
		}
		System.out.println();
		}

		System.out.println("Parallel result:");
		for (int r = 0; r < parResult.length; r++) {
		for (int c = 0; c < parResult[0].length; c++) {
		System.out.print(parResult[r][c]);
		if (c < parResult[0].length - 1) {
		System.out.print(" ");
		}
		}
		System.out.println();
		}

		// Test command for 1.4
		// runThreadScalingExperiment(2000, new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
		
		// Test command for 1.5
		// runSizeScalingExperiment(new int[]{100,200,500,1000,2000,3000,4000}, 7);
	}

	/**
	 * Returns the result of a sequential matrix multiplication
	 * The two matrices are randomly generated
	 * 
	 * @param a is the first matrix
	 * @param b is the second matrix
	 * @return the result of the multiplication
	 */
	public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {
		int aRowCount = a.length;
		int bRowCount = b.length;
		int aColCount = a[0].length;
		int bColCount = b[0].length;
		double[][] result = new double[aRowCount][bColCount];

		if (aColCount != bRowCount) {
			throw new ArithmeticException("Invalid matrix dimensions");
		}

		for (int r = 0; r < aRowCount; r++) {
			for (int c = 0; c < bColCount; c++) {
				for (int k = 0; k < aColCount; k++) {
					result[r][c] += a[r][k] * b[k][c];
				}
			}
		}
		return result;
	}

	/**
	 * Returns the result of a concurrent matrix multiplication
	 * The two matrices are randomly generated
	 * 
	 * @param a is the first matrix
	 * @param b is the second matrix
	 * @return the result of the multiplication
	 */
	public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
		int aRowCount = a.length;
		int bRowCount = b.length;
		int aColCount = a[0].length;
		int bColCount = b[0].length;
		double[][] result = new double[aRowCount][bColCount];

		if (aColCount != bRowCount) {
			throw new ArithmeticException("Invalid matrix dimensions");
		}

		ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
		try {
			for (int r = 0; r < aRowCount; r++) {
				for (int c = 0; c < bColCount; c++) {
					executor.execute(new ParallelMultiply(r, c, a, b, result));
				}
			}
		} finally {
			executor.shutdown();
		}

		try {
			executor.awaitTermination(MATRIX_SIZE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		return result;
	}

	/**
	 * Parallel multiplication with a configurable number of threads.
	 * Each task computes one cell of the output matrix.
	 * 
	 * @param a          first matrix
	 * @param b          second matrix
	 * @param numThreads number of worker threads
	 * @return result of the multiplication
	 */
	public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b, int numThreads) {
		int aRowCount = a.length;
		int bRowCount = b.length;
		int aColCount = a[0].length;
		int bColCount = b[0].length;
		double[][] result = new double[aRowCount][bColCount];

		if (aColCount != bRowCount) {
			throw new ArithmeticException("Invalid matrix dimensions");
		}

		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		try {
			for (int r = 0; r < aRowCount; r++) {
				for (int c = 0; c < bColCount; c++) {
					executor.execute(new ParallelMultiply(r, c, a, b, result));
				}
			}
		} finally {
			executor.shutdown();
		}

		try {
			executor.awaitTermination(MATRIX_SIZE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		return result;
	}

	static class ParallelMultiply implements Runnable {
		private final int rowIndex;
		private final int colIndex;
		private final double[][] a;
		private final double[][] b;
		private final double[][] out;

		ParallelMultiply(final int rowIndex, final int colIndex, final double[][] a, final double[][] b,
				final double[][] out) {
			this.rowIndex = rowIndex;
			this.colIndex = colIndex;
			this.a = a;
			this.b = b;
			this.out = out;
		}

		public void run() {
			for (int k = 0; k < a[0].length; k++) {
				out[rowIndex][colIndex] += a[rowIndex][k] * b[k][colIndex];
			}
		}
	}

	/**
	 * Populates a matrix of given size with randomly generated integers between
	 * 0-10.
	 * 
	 * @param numRows number of rows
	 * @param numCols number of cols
	 * @return matrix
	 */
	private static double[][] generateRandomMatrix(int numRows, int numCols) {
		double matrix[][] = new double[numRows][numCols];
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				matrix[row][col] = (double) ((int) (Math.random() * 10.0));
			}
		}
		return matrix;
	}

	public static long measureSequentialTimeMs(double[][] a, double[][] b) {
		long start = System.nanoTime();
		sequentialMultiplyMatrix(a, b);
		long end = System.nanoTime();
		return (end - start) / 1_000_000L;
	}

	public static long measureParallelTimeMs(double[][] a, double[][] b, int numThreads) {
		long start = System.nanoTime();
		parallelMultiplyMatrix(a, b, numThreads);
		long end = System.nanoTime();
		return (end - start) / 1_000_000L;
	}

	public static boolean resultsMatch(double[][] a, double[][] b, int numThreads) {
		double[][] seq = sequentialMultiplyMatrix(a, b);
		double[][] par = parallelMultiplyMatrix(a, b, numThreads);
		return Arrays.deepEquals(seq, par);
	}

	public static void runThreadScalingExperiment(int matrixSize, int[] threadCounts) {
		double[][] a = generateRandomMatrix(matrixSize, matrixSize);
		double[][] b = generateRandomMatrix(matrixSize, matrixSize);
		System.out.println("threads,timeMs");
		for (int t = 0; t < threadCounts.length; t++) {
			int threads = threadCounts[t];
			long timeMs = measureParallelTimeMs(a, b, threads);
			System.out.println(threads + "," + timeMs);
		}
	}

	public static void runSizeScalingExperiment(int[] sizes, int numThreads) {
		System.out.println("size,seqMs,parMs");
		for (int i = 0; i < sizes.length; i++) {
			int size = sizes[i];
			double[][] a = generateRandomMatrix(size, size);
			double[][] b = generateRandomMatrix(size, size);
			long seqMs = measureSequentialTimeMs(a, b);
			long parMs = measureParallelTimeMs(a, b, numThreads);
			System.out.println(size + "," + seqMs + "," + parMs);
		}
	}

}
