package lyubov_EM;

import java.util.Random;

public class TemClust {

    public TData Data;

    public int K;
    public int L;

    Random rnd;

    public double[][] mu;
    public double[][] R;
    public double[] W;
    public double[] SP;
    public double[][] P;
    public double[][] delta;
    public double[][] x;

    public double eps;
    public double[][] mu1;
    public double[][] R1;
    public double[] W1;

    void InitTemp() {
        mu1 = new double[Data.N][K];
        R1 = new double[Data.N][K];
        W1 = new double[K];
    }

    public TemClust(TData Data) {
        this.Data = Data;
        rnd = new Random();
    }

    //инициализация и запуск алгоритма
    public void Run(int K, double eps, int L) throws RuntimeException {
        this.K = K;
        this.L = L;

        this.eps = eps;

        mu = new double[Data.N][K];
        R = new double[Data.N][K];
        W = new double[K];

        SP = new double[Data.M];
        P = new double[Data.M][K];
        delta = new double[Data.M][K];
        x = new double[Data.M][K];

//инициализация начальными параметрами. Сразу пример почему так:
        // есть 2 кластера (k1, k2) и 5 объектов (o1 o2 o3 o4 o5)
        // тогда мат ожидание кластера k1 такое:
        // k1[по полю номер 1] = ЗНАЧЕНИЮ поля 1 объекта o1
        // k1[по полю номер 2] = ЗНАЧЕНИЮ поля 2 объекта o1
        // ...
        // k1[по полю номер N] = ЗНАЧЕНИЮ поля N объекта o1
        //
        // следующий кластер, чтобы не сталкивать их лбами c первым, и постараться увеличить быстроту расчета, инициализируем его значениями другого объекта,
        // если судьба распорядится, что этот объект еще и в итоге окажется принадлежащим не к 1 кластеру, то алгоритм сойдется быстрее
        // k2[по полю номер 1] = ЗНАЧЕНИЮ поля 1 объекта o2
        // k2[по полю номер 2] = ЗНАЧЕНИЮ поля 2 объекта o2
        // ...
        // k2[по полю номер N] = ЗНАЧЕНИЮ поля N объекта o2
        // Таким образом, изначально мы делаем предположение что 1, 2, ... N кластеру принадлежат по порядку первые 1, 2, ... N объектов
        for (int n = 0; n < Data.N; n++) {
            for (int k = 0; k < K; k++) {
                mu[n][k] = Data.values[k][n];
                R[n][k] = 1;
            }

        }

        //распределение весов (в моем случае равных) каждому кластеру
        for (int k = 0; k < K; k++) {
            W[k] = 1.0 / K;
        }

        double llh = 0;
        //запуск E-M цикла, выход при сходимости
        for (int l = 0; l < L; l++) {
            double llh1 = Step();
            if (Math.abs(llh - llh1) < eps) {
                break;
            }
            llh = llh1;
        }
    }

    double Step() throws RuntimeException {
        InitTemp();
        // E
        double llh = 0; //коэфициент сходимости
        llh = e_step(llh);
        // M
        m_step();
        return llh;
    }

    //обновление параметров модели на основании значений полученных из шага E
    private void m_step() {
        for (int k = 0; k < K; k++) {
            for (int n = 0; n < Data.N; n++) {

                if (W1[k] == 0) {
                    System.out.println(k + " " + W1[k]);
                }
                if (mu1[n][k] != mu1[n][k]) {
                    System.out.println(n + " " + k);
                }
                mu[n][k] = mu1[n][k] / W1[k]; //пересчет мю
            }

            for (int m = 0; m < Data.M; m++) {
                for (int n = 0; n < Data.N; n++) {
                    R1[n][k] += (Data.values[m][n] - mu[n][k]) * (Data.values[m][n] - mu[n][k]) * x[m][k];
                }
            }
        }


        for (int k = 0; k < K; k++) {
            for (int n = 0; n < Data.N; n++) {
                R[n][k] = R1[n][k] / Data.M; //СКО

            }
        }

        for (int k = 0; k < K; k++) {
            W[k] = W1[k] / Data.M; //веса
        }
    }

    private double e_step(double llh) {
        for (int m = 0; m < Data.M; m++) {
            SP[m] = 0;

            for (int k = 0; k < K; k++) {
                delta[m][k] = innerR(m, k);
                P[m][k] = Gauss(m, k, delta[m][k]); //вероятность
                if (P[m][k] != P[m][k]) {
                    System.out.println(P[m][k]);
                }
                SP[m] += P[m][k];
            }
            for (int k = 0; k < K; k++) {
                x[m][k] = P[m][k] / SP[m];

                if (x[m][k] != x[m][k]) {
                    System.out.println(m + " " + k + " " + P[m][k]);
                    System.out.println(m + " " + SP[m]);

                    x[m][k] = 0;
                }
            }

            llh += Math.log(SP[m]); //не я придумала, но протетстировав работает хорошо,
            // действительно когда распределение по кластерам достигает 99.99999% к одному и ко всем остальным 00.000.., то этот параметр это цепляет

            for (int n = 0; n < Data.N; n++) {
                for (int k = 0; k < K; k++) {
                    mu1[n][k] += Data.values[m][n] * x[m][k];
                }
            }

            for (int k = 0; k < K; k++) {
                W1[k] += x[m][k];

            }
        }
        return llh;
    }

    double Gauss(int m, int k, double delta) throws RuntimeException {
        double res = 0;

        double sigma = Sigma(k);

        res = Math.exp(-delta / 2) * W[k] / (Math.pow(2 * Math.PI, Data.N / 2) * sigma);

        return res;
    }

    double Sigma(int k) throws RuntimeException {
        double res = 1;

        for (int n = 0; n < Data.N; n++) {
            res *= R[n][k];

        }

        res = Math.sqrt(res);
        if (res <= 0) {
            // System.out.println("!!!!! сигма  = " + res );
            throw new RuntimeException("Данные не подлежат обработке. Отклонение от нормы для поля № " + k + " при расчтете становятся равны 0. Уберите лишнее поле " + k + " из выборки для возможности кластеризации.");
        }
        return res;
    }

    double innerR(int m, int k) throws RuntimeException{
        double res = 0;

        for (int n = 0; n < Data.N; n++) {
            if (R[n][k] < 1e-17) {
                return 1024;
            }

            res += (Data.values[m][n] - mu[n][k]) * (Data.values[m][n] - mu[n][k]) / R[n][k]; //СКО
        }

        if (res != res) {
            System.out.println("!!!");
            throw new RuntimeException("Данные не подлежат обработке. Отклонение от нормы для поля № " + k + " при расчтете становятся равны 0. Уберите лишнее поле " + k + " из выборки для возможности кластеризации.");

        }

        return res;
    }


}
