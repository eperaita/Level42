import SwiftUI

struct LoadingView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath

    var body: some View {
        ZStack {
            Color(red: 1.0, green: 0.988, blue: 0.0)
                .ignoresSafeArea()

            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: .black))
                .scaleEffect(2.5)
        }
        .navigationBarBackButtonHidden(true)
        .navigationBarHidden(true)
        .statusBarHidden(true)
        // Añade estos 2 modificadores:
        .onAppear {
            // Timeout después de 30 segundos
            DispatchQueue.main.asyncAfter(deadline: .now() + 30) {
                if !viewModel.profileLoaded {
                    navigationPath.removeLast() // Vuelve al login
                }
            }
        }
        .onChange(of: viewModel.profileLoaded) { loaded in
            if loaded {
                navigationPath.append("profile") // Navega al perfil
            }
        }
    }
}