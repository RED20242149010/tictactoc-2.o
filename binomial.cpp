#include <iostream>
using namespace std;

int main() {
    char choice;

    do {
        int n, r;
        
        cout << "Enter n and r: ";
        cin >> n >> r;

        // Logic from image_0688d6.png and image_068913.png
        if (r > n || r < 0) {
            cout << "Invalid input: r must be between 0 and n." << endl;
        } else {
            // Table initialization based on image_0688d6.png
            int c[n + 1][r + 1];

            // Setting base cases: C[i][0] = 1
            for (int i = 0; i <= n; i++) {
                c[i][0] = 1;
            }

            // Setting base cases: C[i][i] = 1
            for (int i = 0; i <= r; i++) {
                c[i][i] = 1;
            }

            // Filling the table using C[i][j] = C[i-1][j] + C[i-1][j-1]
            for (int i = 2; i <= n; i++) {
                for (int j = 1; j < i && j <= r; j++) {
                    c[i][j] = c[i - 1][j] + c[i - 1][j - 1];
                }
            }

            // Output result using c[n][r]
            cout << "C(" << n << ", " << r << ") = " << c[n][r] << endl;
        }

        // The "do-again" logic
        cout << "Do you want to calculate again? (y/n): ";
        cin >> choice;

    } while (choice == 'y' || choice == 'Y');

    cout << "Program exited." << endl;
    return 0;
}