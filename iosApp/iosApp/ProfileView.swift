import SwiftUI
import shared

struct ProfileView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath
    let profile = SessionManager.shared.userProfile

    private let customYellow = Color(red: 1.0, green: 0.988, blue: 0.0)
    private let customBlack = Color.black

    // Styles
    private let avatarSize: CGFloat = 220
    private let buttonHeight: CGFloat = 50
    private let logoutButtonSize: CGFloat = 100
    private let horizontalPadding: CGFloat = 40
    private let buttonSpacing: CGFloat = 40

    var body: some View {

    // Protección contra perfil nulo
    if SessionManager.shared.userProfile == nil {
        EmptyView()
            .onAppear {
                navigationPath.removeLast(navigationPath.count)
                navigationPath.append("login")
            }
    } else {
        ZStack {
            customBlack.ignoresSafeArea()

            GeometryReader { geometry in
                    VStack(spacing: 0) {
                        //  AVATAR
                        VStack {
                            ZStack {
                                Circle()
                                    .fill(customYellow)
                                    .frame(width: avatarSize, height: avatarSize)

                                if let imageUrl = profile?.image?.link, let url = URL(string: imageUrl) {
                                    AsyncImage(url: url) { phase in
                                        if case .success(let image) = phase {
                                            image
                                                .resizable()
                                                .scaledToFill()
                                                .frame(width: avatarSize, height: avatarSize)
                                                .clipShape(Circle())
                                        } else {
                                            Circle().fill(Color.gray)
                                        }
                                    }
                                } else {
                                    Circle().fill(Color.gray)
                                }
                            }
                            .padding(.top, 90)
                            .padding(.bottom, 30)
                        }
                        .padding(.top, UIApplication.shared.windows.first?.safeAreaInsets.top ?? 0)
                        .padding(.bottom, 30)

                        //  INFO
                        VStack(spacing: 12) {
                            if let profile = profile {
                                Text(profile.login)
                                    .font(.system(size: 22, weight: .bold))
                                    .padding(.bottom, 4)

                                Text("\(profile.first_name ?? "") \(profile.last_name ?? "")")
                                    .font(.system(size: 16))

                                Text("email: \(profile.email)")
                                    .font(.system(size: 16))

                                Text("Level: \(profile.level ?? 0)")
                                    .font(.system(size: 16))

                                Text("Wallet: \(profile.wallet ?? 0)")
                                    .font(.system(size: 16))
                            }
                        }
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 40)
                        .padding(.bottom, 50)


                        // BOTONES
                        VStack(spacing: 30) {
                            customButton(
                                title: "PROJECTS",
                                action: {
                                    viewModel.loadProjects()
                                    navigationPath.append("projects")
                                }
                            )

                            customButton(
                                title: "SKILLS",
                                action: {
                                    navigationPath.append("skills")
                                }
                            )



                            // Botón LOGOUT
                            Button(action: {
                                // 1. Resetear ViewModel
                                    viewModel.profileLoaded = false

                                    // 2. Limpiar sesión
                                    SessionManager.shared.clearSession()

                                    // 3. Navegar a login
                                    navigationPath.removeLast(navigationPath.count)
                                    navigationPath.append("login")
                            }) {
                                Text("LOG OUT")
                                    .font(.system(size: 16, weight: .black))
                                    .frame(width: logoutButtonSize, height: logoutButtonSize)
                                    .background(customYellow)
                                    .clipShape(Circle())
                                    .overlay(
                                        Circle()
                                            .stroke(customBlack, lineWidth: 2)
                                    )
                            }
                            .foregroundColor(customBlack)
                            .padding(.bottom, 30)
                        }
                        .padding(.horizontal,40)
                        .padding(.bottom, 50)
                    }
                    .frame(width: geometry.size.width, height: geometry.size.height)
                }
            }
        .edgesIgnoringSafeArea(.all)
        .navigationBarBackButtonHidden(true)
        .navigationBarHidden(true)
        }
    }

    // Función para botones custom
    private func customButton(title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 18, weight: .bold))
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .frame(maxWidth: .infinity)
                .frame(height: buttonHeight)
                .foregroundColor(customYellow)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(customYellow, lineWidth: 2)
                )
        }
    }
}
