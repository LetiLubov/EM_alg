package lyubov_EM;

import java.util.Random;

public class Tem {

    public int N;//количество объектов
    public int M;//количество полей объекта
    public int clustersNumber;//количество кластеров
    public int L;//ограничение на шаги

    public TData Data;

    double[] W; //веса
    double[][] mu; //мю
    double[][] s; //сигма
    double[][] g; //вероятность принадлежности

    public double[][] G() {
        return g;
    }

    public  Tem(TData data, int clustersNumber, int L)throws RuntimeException {
        this.Data = data;
        this.clustersNumber = clustersNumber;
        this.L = L;
        N = Data.N;
        M = Data.M;
        Run();
    }

    void Run()throws RuntimeException {
        Init();
        for (int l = 0; l < L; l++) {
            Step();
        }
    }
    void Step() throws RuntimeException{
        // E
        e_step();
        // M
        m_step();
    }
    //expectation
    // на этом шаге расчет вероятности для присвоения данных кластеру (отдельно по каждому из полей объекта)
    private void e_step() {
        for (int k = 0; k < clustersNumber; k++) {
            for (int m = 0; m < M; m++) {
                double b = 0;
                //знаменатель из формулы
                for (int k2 = 0; k2 < clustersNumber; k2++) {
                    b += W[k2] * FuncPropability(k2, m); //вес * вероятность
                }
                g[k][m] =W[k] * FuncPropability(k, m) / b;
            }
        }
    }
    //maximization
    private void m_step() {
        for (int k = 0; k < clustersNumber; k++) {

            //пересчет весов
            W[k] = 0;
            for (int m = 0; m < M; m++) {
                W[k] += g[k][ m];
            }
            W[k] /= M;

            for (int n = 0; n < N; n++) {
                mu[k][n] =0;

                for (int m = 0; m < M; m++) {
                    mu[k][n] +=g[k][m] *Data.values[m][n];
                }

                mu[k][n] /=M * W[k];//пересчет мю
            }
        }

        for (int k = 0; k < clustersNumber; k++) {
            for (int n = 0; n < N; n++) {
               // s[k][n] =0;
                for (int m = 0; m < M; m++) {
                    s[k][n] +=g[k][m] *(Data.values[m][n]-mu[k][n]) *(Data.values[m][n]-mu[k][n]);
                }

                s[k][n] /=M * W[k];//пересчет сигм
            }
        }
    }
    //расчет  вероятности
    double FuncPropability(int k, int m) throws RuntimeException{
        double res = 0;

        double s1 = 1;
        //сумма k = произведение сигм
        for (int n = 0; n < N; n++) {

            if (s[k][n] <= 0) {
               // System.out.println("сигма  = " + s[k][n] + " k = " + k + " n = " + n);
               //s[k][n] = 1;
                throw new RuntimeException("Данные не подлежат обработке. Отклонение от нормы для поля № " + k + " при расчтете становятся равны 0. Уберите лишнее поле " + k + " из выборки для возможности кластеризации.");
            }
            s1 *= s[k][n];
        }
        for (int n = 0; n < N; n++) {
            res += (Data.values[m][n]-mu[k][n]) *(Data.values[m][n]-mu[k][n]) /s[k][n];
        }
        res = Math.exp(-res / 2);
        res /= Math.sqrt(s1);
        res /= Math.pow(2 * Math.PI, N / 2);

        return res;
    }
    //Инициализация перед началом работы алгоритма
    //распределение весов (в моем случае равных) каждому кластеру
    //мат ожидание
    void Init() {
        Random rnd = new Random();

        W = new double[clustersNumber];
        for (int k = 0; k < clustersNumber; k++) {
            W[k] = 1.0 / clustersNumber;
        }

        mu = new double[clustersNumber][N];
        //сразу пример:
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
        for (int k = 0; k < clustersNumber; k++) {
            for (int n = 0; n < N; n++) {
                mu[k][n] =Data.values[k][n];
            }
        }

        //считаем СКО в соответствии с мю
        s = new double[clustersNumber][ N];
        for (int k = 0; k < clustersNumber; k++) {
            for (int n = 0; n < N; n++) {
                s[k][n] =0;

                for (int m = 0; m < M; m++) {
                    s[k][n] +=(Data.values[m][n]-mu[k][n]) *(Data.values[m][n]-mu[k][n]);
                }
                s[k][n] /=M * clustersNumber;
               // s[k][n] =1;
            }
        }
        //объявление вероятности
        g = new double[clustersNumber][M];
    }

}
