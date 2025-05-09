import SwiftUI
import shared

struct LoadingView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath
    @State private var showTimeoutMessage = false
    private let timeoutDuration: Double = 30 // 30 segundos 

    var body: some View {
        ZStack {
            // Fondo amarillo (#FFFFC00)
            Color(red: 1.0, green: 0.988, blue: 0.0)
                .ignoresSafeArea()

            if showTimeoutMessage {
                VStack {
                    Text("Timeout occurred")
                        .foregroundColor(.black)
                    
                    Button("Retry") {
                        showTimeoutMessage = false
                        startLoading()
                    }
                    .padding()
                    .background(Color.black)
                    .foregroundColor(Color(red: 1.0, green: 0.988, blue: 0.0))
                    .cornerRadius(8)
                }
            } else {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .black))
                    .scaleEffect(2.5)
            }
        }
        .navigationBarBackButtonHidden(true)
        .navigationBarHidden(true)
        .onAppear {
            startLoading()
        }
        .onChange(of: viewModel.authState) { newState in
            handleAuthStateChange(newState)
        }
    }

    private func startLoading() {
        DispatchQueue.main.asyncAfter(deadline: .now() + timeoutDuration) {
            if case .idle = viewModel.authState {
                showTimeoutMessage = true
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    navigationPath.removeLast()
                    navigationPath.append("login")
                }
            }
        }
    }

    private func handleAuthStateChange(_ state: ProfileViewModel.AuthState) {
        if case .success = state {
            navigationPath.removeLast()
            navigationPath.append("welcome")
        } else if case .error = state {
            navigationPath.removeLast()
            navigationPath.append("login")
        }
    }
}
